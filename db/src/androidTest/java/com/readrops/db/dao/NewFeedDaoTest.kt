package com.readrops.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewFeedDaoTest {

    private lateinit var database: Database
    private lateinit var account: Account

    @Before
    fun before() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()

        account = Account(accountType = AccountType.LOCAL).apply {
            id = database.newAccountDao().insert(this).toInt()
        }

        repeat(2) { time ->
            database.newFolderDao().insert(
                Folder(
                    name = "Folder $time",
                    remoteId = "folder_$time",
                    accountId = account.id
                )
            )
        }

        repeat(3) { time ->
            database.newFeedDao().insert(
                Feed(
                    name = "Feed $time",
                    remoteId = "feed_$time",
                    remoteFolderId = "folder_${if (time % 2 == 0) 0 else 1}",
                    accountId = account.id
                )
            )
        }
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun upsertFeedsTest() = runTest {
        val newFeeds = listOf(
            // updated feed (name + folder to null)
            Feed(
                name = "New Feed 0",
                remoteId = "feed_0",
                remoteFolderId = null,
                accountId = account.id
            ),

            // deleted feed
            /*Feed(
                name = "Feed 1",
                remoteId = "feed_1",
                remoteFolderId = "folder_1",
                accountId = account.id
            ),*/

            // updated feed (folder change)
            Feed(
                name = "Feed 2",
                remoteId = "feed_2",
                remoteFolderId = "folder_1",
                accountId = account.id
            ),

            // inserted feed
            Feed(
                name = "Feed 3",
                remoteId = "feed_3",
                remoteFolderId = "folder_0",
                accountId = account.id
            ),
        )

        database.newFeedDao().upsertFeeds(newFeeds, account)
        val allFeeds = database.newFeedDao().selectFeeds(account.id)

        assertTrue(allFeeds.any { it.name == "New Feed 0" && it.folderId == null })
        assertTrue(allFeeds.any { it.remoteId == "feed_2" && it.folderId == 2 })

        assertFalse(allFeeds.any { it.remoteId == "feed_1" })
        assertTrue(allFeeds.any { it.remoteId == "feed_3" && it.folderId == 1 })
    }
}