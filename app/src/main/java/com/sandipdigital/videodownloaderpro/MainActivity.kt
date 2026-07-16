package com.sandipdigital.videodownloaderpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sandipdigital.videodownloaderpro.data.datastore.SettingsDataStore
import com.sandipdigital.videodownloaderpro.ui.navigation.AppNavGraph
import com.sandipdigital.videodownloaderpro.ui.navigation.Destination
import com.sandipdigital.videodownloaderpro.ui.theme.VideoDownloaderProTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsDataStore.settingsFlow.collectAsState(
                initial = com.sandipdigital.videodownloaderpro.data.datastore.AppSettings()
            )

            VideoDownloaderProTheme(
                themeMode = settings.themeMode,
                useDynamicColor = settings.useDynamicColor
            ) {
                MainScaffold()
            }
        }
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            // Hide bottom bar on the full-screen player.
            if (currentRoute?.startsWith("player/") != true) {
                NavigationBar {
                    Destination.bottomNavItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(navController = navController)
        Modifier.padding(innerPadding) // padding consumed inside each screen's own Scaffold
    }
}
