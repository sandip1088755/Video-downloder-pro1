package com.sandipdigital.videodownloaderpro.ui.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalScaffold("Privacy Policy", onBack) {
        Text(
            """
            Video Downloader Pro respects your privacy.

            • This app does not collect, transmit, or sell your personal data to any server operated by us.
            • Links you paste are used only to fetch file metadata (name, size, type) directly from the source server you specify, and to download the file you request.
            • Downloaded files are stored in app-specific storage on your device and are not uploaded anywhere by this app.
            • Standard device permissions (notifications, media access) are used only to show download progress and to let you open/share files you've downloaded.
            • This app does not display third-party ads and does not include ad-tracking SDKs.

            If you have questions about this policy, please contact the developer through the Play Store listing.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    LegalScaffold("Terms of Service", onBack) {
        Text(
            """
            By using Video Downloader Pro, you agree to the following:

            • This app is a general-purpose direct-link download manager. It downloads files only from the exact HTTPS URL you provide.
            • You are solely responsible for ensuring you have the legal right to download and use any file you request through this app.
            • Do not use this app to download content that infringes copyright or violates the terms of service of any third-party website.
            • The app is provided "as is" without warranty of any kind. The developer is not liable for any misuse of the app or for content downloaded by users.
            • These terms may be updated from time to time; continued use of the app constitutes acceptance of the current terms.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    LegalScaffold("About", onBack) {
        Text("Video Downloader Pro", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "A fast, secure download manager for direct HTTPS video and audio links, built with Kotlin, Jetpack Compose, and Material 3.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Developed by Sandip Digital", style = MaterialTheme.typography.bodyMedium)
    }
}
