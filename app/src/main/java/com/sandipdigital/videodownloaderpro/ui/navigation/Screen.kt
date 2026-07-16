package com.sandipdigital.videodownloaderpro.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Downloads : Screen("downloads")
    object Files : Screen("files")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Privacy : Screen("privacy")
    object Terms : Screen("terms")
    object About : Screen("about")

    object Player : Screen("player/{downloadId}") {
        fun createRoute(downloadId: Long) = "player/$downloadId"
    }
}

val bottomNavScreens = listOf(Screen.Home, Screen.Downloads, Screen.Files, Screen.History)
