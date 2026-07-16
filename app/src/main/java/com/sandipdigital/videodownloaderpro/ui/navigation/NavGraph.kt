package com.sandipdigital.videodownloaderpro.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sandipdigital.videodownloaderpro.ui.screens.downloads.DownloadsScreen
import com.sandipdigital.videodownloaderpro.ui.screens.files.FilesScreen
import com.sandipdigital.videodownloaderpro.ui.screens.home.HomeScreen
import com.sandipdigital.videodownloaderpro.ui.screens.player.PlayerScreen
import com.sandipdigital.videodownloaderpro.ui.screens.settings.SettingsScreen
import java.net.URLDecoder

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) + slideInHorizontally { it / 6 } },
        exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 6 } }
    ) {
        composable(Destination.Home.route) {
            HomeScreen(onOpenPlayer = { path, isAudio ->
                navController.navigate(playerRoute(java.net.URLEncoder.encode(path, "UTF-8"), isAudio))
            })
        }
        composable(Destination.Downloads.route) {
            DownloadsScreen(onOpenPlayer = { path, isAudio ->
                navController.navigate(playerRoute(java.net.URLEncoder.encode(path, "UTF-8"), isAudio))
            })
        }
        composable(Destination.Files.route) {
            FilesScreen(onOpenPlayer = { path, isAudio ->
                navController.navigate(playerRoute(java.net.URLEncoder.encode(path, "UTF-8"), isAudio))
            })
        }
        composable(Destination.Settings.route) {
            SettingsScreen()
        }
        composable(ROUTE_PLAYER) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("encodedPath").orEmpty()
            val isAudio = backStackEntry.arguments?.getString("isAudio")?.toBoolean() ?: false
            val path = URLDecoder.decode(encoded, "UTF-8")
            PlayerScreen(filePath = path, isAudio = isAudio, onBack = { navController.popBackStack() })
        }
    }
}
