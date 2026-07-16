package com.sandipdigital.videodownloaderpro.ui.screens.downloads

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.domain.model.DownloadItem
import com.sandipdigital.videodownloaderpro.domain.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onOpenPlayer: (String, Boolean) -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val active by viewModel.activeDownloads.collectAsState()
    val completed by viewModel.completedDownloads.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(topBar = { TopAppBar(title = { Text("Downloads", fontWeight = FontWeight.SemiBold) }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Active (${active.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Completed (${completed.size})") })
            }

            val list = if (selectedTab == 0) active else completed
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (selectedTab == 0) "No active downloads" else "Nothing downloaded yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { item ->
                        DownloadRow(
                            item = item,
                            onPause = { viewModel.pause(item.id) },
                            onResume = { viewModel.resume(item) },
                            onRetry = { viewModel.retry(item.id) },
                            onCancel = { viewModel.cancel(item.id) },
                            onDelete = { viewModel.delete(item.id, deleteFile = true) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id, !item.isFavorite) },
                            onOpen = { onOpenPlayer(item.filePath, item.mediaType == MediaType.AUDIO) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (item.mediaType == MediaType.AUDIO) Icons.Filled.AudioFile else Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(item.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.status == DownloadStatus.RUNNING || item.status == DownloadStatus.PAUSED) {
                LinearProgressIndicator(
                    progress = { item.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.progressPercent}%", style = MaterialTheme.typography.labelSmall)
                    val speedText = formatSpeed(item.speedBytesPerSec)
                    val etaText = formatEta(item.etaSeconds)
                    Text("$speedText · ETA $etaText", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (item.status == DownloadStatus.FAILED) {
                Text("Failed — tap retry", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (item.status) {
                    DownloadStatus.RUNNING -> {
                        AssistChip(onClick = onPause, label = { Text("Pause") }, leadingIcon = { Icon(Icons.Filled.Pause, null) })
                        AssistChip(onClick = onCancel, label = { Text("Cancel") }, leadingIcon = { Icon(Icons.Filled.Close, null) })
                    }
                    DownloadStatus.PAUSED -> {
                        AssistChip(onClick = onResume, label = { Text("Resume") }, leadingIcon = { Icon(Icons.Filled.PlayArrow, null) })
                        AssistChip(onClick = onCancel, label = { Text("Cancel") }, leadingIcon = { Icon(Icons.Filled.Close, null) })
                    }
                    DownloadStatus.FAILED -> {
                        AssistChip(onClick = onRetry, label = { Text("Retry") }, leadingIcon = { Icon(Icons.Filled.Refresh, null) })
                        AssistChip(onClick = onDelete, label = { Text("Remove") }, leadingIcon = { Icon(Icons.Filled.Delete, null) })
                    }
                    DownloadStatus.QUEUED -> {
                        AssistChip(onClick = onCancel, label = { Text("Cancel") }, leadingIcon = { Icon(Icons.Filled.Close, null) })
                    }
                    DownloadStatus.COMPLETED -> {
                        AssistChip(onClick = onOpen, label = { Text("Play") }, leadingIcon = { Icon(Icons.Filled.PlayArrow, null) })
                        AssistChip(onClick = onDelete, label = { Text("Delete") }, leadingIcon = { Icon(Icons.Filled.Delete, null) })
                    }
                    DownloadStatus.CANCELLED -> {
                        AssistChip(onClick = onDelete, label = { Text("Remove") }, leadingIcon = { Icon(Icons.Filled.Delete, null) })
                    }
                }
            }
        }
    }
}

private fun formatSpeed(bytesPerSec: Long): String = when {
    bytesPerSec <= 0 -> "-- KB/s"
    bytesPerSec < 1024 * 1024 -> "%.0f KB/s".format(bytesPerSec / 1024.0)
    else -> "%.1f MB/s".format(bytesPerSec / (1024.0 * 1024.0))
}

private fun formatEta(seconds: Long): String = when {
    seconds < 0 -> "--"
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}
