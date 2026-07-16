package com.sandipdigital.videodownloaderpro.ui.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.ui.components.DownloadItemCard

@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) {
    val downloads by viewModel.activeDownloads.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Downloads", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No active downloads. Paste a link on the Home tab to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(downloads, key = { it.id }) { entity ->
                    DownloadItemCard(
                        entity = entity,
                        onPause = { viewModel.pause(entity.id) },
                        onResume = { viewModel.resume(entity.id) },
                        onCancel = { viewModel.cancel(entity.id) },
                        onRetry = { viewModel.retry(entity.id) }
                    )
                }
            }
        }
    }
}
