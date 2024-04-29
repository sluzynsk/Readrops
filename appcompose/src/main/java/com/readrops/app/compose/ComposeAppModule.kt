package com.readrops.app.compose

import com.readrops.app.compose.account.AccountScreenModel
import com.readrops.app.compose.account.credentials.AccountCredentialsScreenModel
import com.readrops.app.compose.account.selection.AccountSelectionScreenModel
import com.readrops.app.compose.feeds.FeedScreenModel
import com.readrops.app.compose.item.ItemScreenModel
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.app.compose.repositories.LocalRSSRepository
import com.readrops.app.compose.timelime.TimelineScreenModel
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import org.koin.dsl.module

val composeAppModule = module {

    factory { TimelineScreenModel(get(), get()) }

    factory { FeedScreenModel(get(), get(), get()) }

    factory { AccountSelectionScreenModel(get()) }

    factory { AccountScreenModel(get()) }

    factory { (itemId: Int) ->
        ItemScreenModel(get(), itemId)
    }

    factory { (accountType: AccountType) -> AccountCredentialsScreenModel(accountType) }

    single { GetFoldersWithFeeds(get()) }

    // repositories

    factory<BaseRepository> { (account: Account) ->
        LocalRSSRepository(get(), get(), account)
    }

}