package com.readrops.app.compose.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncResult
import com.readrops.api.services.SyncType
import com.readrops.api.services.freshrss.FreshRSSSyncData
import com.readrops.api.services.freshrss.NewFreshRSSDataSource
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.compose.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account
import org.joda.time.DateTime
import org.koin.core.component.KoinComponent

class FreshRSSRepository(
    database: Database,
    account: Account,
    private val dataSource: NewFreshRSSDataSource,
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        val authInterceptor = getKoin().get<AuthInterceptor>()
        authInterceptor.credentials = Credentials.toCredentials(account)

        account.token = dataSource.login(account.login!!, account.password!!)
        // we got the authToken, time to provide it to make real calls
        authInterceptor.credentials = Credentials.toCredentials(account)

        account.writeToken = dataSource.getWriteToken()

        val userInfo = dataSource.getUserInfo()
        account.displayedName = userInfo.userName
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    override suspend fun synchronize(): SyncResult {
        val itemStateChanges = database.newItemStateChangeDao()
            .selectItemStateChanges(account.id)

        val syncData = FreshRSSSyncData(
            readIds = itemStateChanges.filter { it.readChange && it.read }
                .map { it.remoteId },
            unreadIds = itemStateChanges.filter { it.readChange && !it.read }
                .map { it.remoteId },
            starredIds = itemStateChanges.filter { it.starChange && it.starred }
                .map { it.remoteId },
            unstarredIds = itemStateChanges.filter { it.starChange && !it.starred }
                .map { it.remoteId }
        )

        val syncType: SyncType
        if (account.lastModified != 0L) {
            syncType = SyncType.CLASSIC_SYNC
            syncData.lastModified = account.lastModified
        } else {
            syncType = SyncType.INITIAL_SYNC
        }

        val newLastModified = DateTime.now().millis / 1000L

        return dataSource.synchronize(syncType, syncData, account.writeToken!!).apply {
            insertFolders(folders)
            insertFeeds(feeds)

            insertItems(items, false)
            insertItems(starredItems, true)

            insertItemsIds(unreadIds, readIds, starredIds.toMutableList())

            account.lastModified = newLastModified
            database.newAccountDao().updateLastModified(newLastModified, account.id)

            database.newItemStateChangeDao().resetStateChanges(account.id)
        }
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult = TODO("Not yet implemented")

    override suspend fun updateFolder(folder: Folder) {
        dataSource.updateFolder(account.writeToken!!, folder.remoteId!!, folder.name!!)
        folder.remoteId = NewFreshRSSDataSource.FOLDER_PREFIX + folder.name

        super.updateFolder(folder)
    }

    override suspend fun deleteFolder(folder: Folder) {
        dataSource.deleteFolder(account.writeToken!!, folder.remoteId!!)
        super.deleteFolder(folder)
    }

    private suspend fun insertFeeds(feeds: List<Feed>) {
        feeds.forEach { it.accountId = account.id }
        database.newFeedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.newFolderDao().upsertFolders(folders, account)
    }

    private suspend fun insertItems(items: List<Item>, starredItems: Boolean) {
        val itemsToInsert = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String?, Int>()

        for (item in items) {
            val feedId: Int
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds.getValue(item.feedRemoteId)
            } else {
                feedId =
                    database.newFeedDao().selectRemoteFeedLocalId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId] = feedId
            }

            item.feedId = feedId

            if (item.text != null) {
                item.readTime = Utils.readTimeFromString(item.text!!)
            }

            // workaround to avoid inserting starred items coming from the main item call
            // as the API exclusion filter doesn't seem to work
            if (!starredItems) {
                if (!item.isStarred) {
                    itemsToInsert.add(item)
                }
            } else {
                itemsToInsert.add(item)
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            itemsToInsert.sortWith(Item::compareTo)
            database.itemDao().insert(itemsToInsert)
        }
    }

    private suspend fun insertItemsIds(
        unreadIds: List<String>,
        readIds: List<String>,
        starredIds: MutableList<String> // TODO is it performance wise?
    ) {
        database.newItemStateDao().deleteItemStates(account.id)

        database.newItemStateDao().insert(unreadIds.map { id ->
            val starred = starredIds.count { starredId -> starredId == id } == 1

            if (starred) {
                starredIds.remove(id)
            }

            ItemState(
                id = 0,
                read = false,
                starred = starred,
                remoteId = id,
                accountId = account.id
            )
        })

        database.newItemStateDao().insert(readIds.map { id ->
            val starred = starredIds.count { starredId -> starredId == id } == 1
            if (starred) {
                starredIds.remove(id)
            }

            ItemState(
                id = 0,
                read = true,
                starred = starred,
                remoteId = id,
                accountId = account.id
            )
        })

        // insert starred items ids which are read
        if (starredIds.isNotEmpty()) {
            database.newItemStateDao().insert(starredIds.map { id ->
                ItemState(
                    0,
                    read = true,
                    starred = true,
                    remoteId = id,
                    accountId = account.id
                )
            })
        }
    }
}