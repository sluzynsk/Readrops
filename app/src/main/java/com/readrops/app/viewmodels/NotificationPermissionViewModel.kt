package com.readrops.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Feed
import com.readrops.readropsdb.entities.account.Account
import io.reactivex.Completable

class NotificationPermissionViewModel(application: Application) : AndroidViewModel(application) {

    val database = Database.getInstance(application)
    var account: Account? = null

    fun getAccount(accountId: Int) = database.accountDao().selectAsync(accountId)

    fun getFeedsWithNotifPermission(): LiveData<List<Feed>> = database.feedDao()
            .getFeedsForNotifPermission(account?.id!!)

    fun setAccountNotificationsState(enabled: Boolean): Completable = database.accountDao()
            .updateNotificationState(account?.id!!, enabled)

    fun setFeedNotificationState(feed: Feed): Completable = database.feedDao()
            .updateNotificationState(feed.id, !feed.isNotificationEnabled)
}