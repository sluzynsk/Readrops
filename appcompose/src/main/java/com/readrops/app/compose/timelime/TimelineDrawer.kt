package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.readrops.app.compose.R
import com.readrops.app.compose.util.theme.spacing

enum class DrawerDefaultItemsSelection {
    ARTICLES,
    NEW,
    FAVORITES,
    READ_LATER
}

@Composable
fun TimelineDrawer(
    viewModel: TimelineViewModel,
    onClickDefaultItem: (DrawerDefaultItemsSelection) -> Unit,
) {
    val state by viewModel.drawerState.collectAsState()

    ModalDrawerSheet {
        Spacer(modifier = Modifier.size(MaterialTheme.spacing.drawerSpacing))

        DrawerDefaultItems(
            selectedItem = state.selection,
            onClick = { onClickDefaultItem(it) }
        )

        DrawerDivider()
    }
}

@Composable
fun DrawerDefaultItems(
    selectedItem: DrawerDefaultItemsSelection,
    onClick: (DrawerDefaultItemsSelection) -> Unit,
) {
    NavigationDrawerItem(
        label = { Text("Articles") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_timeline),
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.ARTICLES,
        onClick = { onClick(DrawerDefaultItemsSelection.ARTICLES) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("New articles") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_new),
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.NEW,
        onClick = { onClick(DrawerDefaultItemsSelection.NEW) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("Favorites") },
        icon = {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.FAVORITES,
        onClick = { onClick(DrawerDefaultItemsSelection.FAVORITES) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("To read later") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_read_later),
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.READ_LATER,
        onClick = { onClick(DrawerDefaultItemsSelection.READ_LATER) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun DrawerDivider() {
    Divider(
        thickness = 2.dp,
        modifier = Modifier.padding(
            vertical = MaterialTheme.spacing.drawerSpacing,
            horizontal = 28.dp // M3 guidelines
        )
    )
}