package com.sandipdigital.videodownloaderpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Destination("home", "Home", Icons.Filled.Home)
    data object Downloads : Destination("downloads", "Downloads", Icons.Filled.Download)
    data object Files : Destination("files", "Files", Icons.Filled.Folder)
    data object Settings : Destination("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Downloads, Files, Settings)
    }
}

const val ROUTE_PLAYER = "player/{encodedPath}/{isAudio}"
fun playerRoute(encodedPath: String, isAudio: Boolean) = "player/$encodedPath/$isAudio"
