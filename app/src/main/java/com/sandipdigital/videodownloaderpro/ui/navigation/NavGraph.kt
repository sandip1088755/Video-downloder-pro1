package com.sandipdigital.videodownloaderpro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sandipdigital.videodownloaderpro.ui.RootViewModel
import com.sandipdigital.videodownloaderpro.ui.components.MainScaffold
import com.sandipdigital.videodownloaderpro.ui.screens.downloads.DownloadsScreen
import com.sandipdigital.videodownloaderpro.ui.screens.filemanager.FileManagerScreen
import com.sandipdigital.videodownloaderpro.ui.screens.history.HistoryScreen
import com.sandipdigital.videodownloaderpro.ui.screens.home.HomeScreen
import com.sandipdigital.videodownloaderpro.ui.screens.legal.AboutScreen
import com.sandipdigital.videodownloaderpro.ui.screens.legal.PrivacyPolicyScreen
import com.sandipdigital.videodownloaderpro.ui.screens.legal.TermsScreen
import com.sandipdigital.videodownloaderpro.ui.screens.onboarding.OnboardingScreen
import com.sandipdigital.videodownloaderpro.ui.screens.player.PlayerScreen
import com.sandipdigital.videodownloaderpro.ui.screens.settings.SettingsScreen

@Composable
fun AppRoot() {
    val rootViewModel: RootViewModel = hiltViewModel()
    val onboardingDone by rootViewModel.onboardingDone.collectAsState()

    // Wait for the DataStore read before deciding the start destination
    when (onboardingDone) {
        null -> { /* brief loading frame while preferences load */ }
        false -> OnboardingScreen(onFinished = { rootViewModel.completeOnboarding() })
        true -> MainNavHost()
    }
}

@Composable
private fun MainNavHost() {
    val navController: NavHostController = rememberNavController()

    MainScaffold(navController) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onDownloadStarted = {
                    navController.navigate(Screen.Downloads.route) {
                        launchSingleTop = true
                    }
                })
            }
            composable(Screen.Downloads.route) { DownloadsScreen() }
            composable(Screen.Files.route) { FileManagerScreen() }
            composable(Screen.History.route) {
                HistoryScreen(onOpenPlayer = { id ->
                    navController.navigate(Screen.Player.createRoute(id))
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onOpenPrivacy = { navController.navigate(Screen.Privacy.route) },
                    onOpenTerms = { navController.navigate(Screen.Terms.route) },
                    onOpenAbout = { navController.navigate(Screen.About.route) }
                )
            }
            composable(Screen.Privacy.route) { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Terms.route) { TermsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Player.route) {
                PlayerScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
