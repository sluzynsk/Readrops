package com.readrops.app.compose.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.filters.MainFilter
import com.readrops.db.queries.FeedUnreadItemsCountQueryBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFoldersWithFeeds(
    private val database: Database,
) {

    fun get(accountId: Int, mainFilter: MainFilter): Flow<Map<Folder?, List<Feed>>> {
        val query = FeedUnreadItemsCountQueryBuilder.build(accountId, mainFilter)

        return combine(
            flow = database.newFolderDao().selectFoldersAndFeeds(accountId),
            flow2 = database.newItemDao().selectFeedUnreadItemsCount(query)
        ) { folders, itemCounts ->
            val foldersWithFeeds = folders.groupBy(
                keySelector = {
                    if (it.folderId != null) {
                        Folder(
                            id = it.folderId!!,
                            name = it.folderName,
                            remoteId = it.folderRemoteId,
                            accountId = it.accountId
                        )
                    } else {
                        null
                    }
                },
                valueTransform = {
                    Feed(
                        id = it.feedId,
                        name = it.feedName,
                        iconUrl = it.feedIcon,
                        url = it.feedUrl,
                        siteUrl = it.feedSiteUrl,
                        description = it.feedDescription,
                        unreadCount = itemCounts[it.feedId] ?: 0
                    )
                }
            ).mapValues { listEntry ->
                // Empty folder case
                if (listEntry.value.any { it.id == 0 }) {
                    listOf()
                } else {
                    listEntry.value
                }
            }

            foldersWithFeeds.toSortedMap(nullsLast(Folder::compareTo))
        }
    }
}