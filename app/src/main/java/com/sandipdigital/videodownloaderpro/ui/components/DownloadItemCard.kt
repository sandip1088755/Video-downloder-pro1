package com.sandipdigital.videodownloaderpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandipdigital.videodownloaderpro.data.local.DownloadEntity
import com.sandipdigital.videodownloaderpro.data.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.util.FileUtils

@Composable
fun DownloadItemCard(
    entity: DownloadEntity,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    onRetry: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val progress = if (entity.totalBytes > 0)
        (entity.downloadedBytes.toFloat() / entity.totalBytes.toFloat()).coerceIn(0f, 1f)
    else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    entity.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(entity.status)
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${(progress * 100).toInt()}% • ${FileUtils.humanReadableBytes(entity.downloadedBytes)} / ${FileUtils.humanReadableBytes(entity.totalBytes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entity.status == DownloadStatus.RUNNING) {
                    Text(
                        "${FileUtils.humanReadableSpeed(entity.speedBps)} • ETA ${FileUtils.formatEta(entity.etaSeconds)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entity.status == DownloadStatus.FAILED && entity.errorMessage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    entity.errorMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (entity.status) {
                    DownloadStatus.RUNNING, DownloadStatus.QUEUED -> {
                        OutlinedButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Pause")
                        }
                    }
                    DownloadStatus.PAUSED -> {
                        OutlinedButton(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Resume")
                        }
                    }
                    DownloadStatus.FAILED -> {
                        OutlinedButton(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Retry")
                        }
                    }
                    else -> {}
                }
                OutlinedButton(onClick = onCancel) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp)); Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: DownloadStatus) {
    val (label, color) = when (status) {
        DownloadStatus.RUNNING -> "Downloading" to MaterialTheme.colorScheme.primary
        DownloadStatus.QUEUED -> "Queued" to MaterialTheme.colorScheme.tertiary
        DownloadStatus.PAUSED -> "Paused" to MaterialTheme.colorScheme.secondary
        DownloadStatus.COMPLETED -> "Done" to MaterialTheme.colorScheme.primary
        DownloadStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        DownloadStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.outline
    }
    AssistChip(onClick = {}, label = { Text(label, style = MaterialTheme.typography.labelMedium) })
}
