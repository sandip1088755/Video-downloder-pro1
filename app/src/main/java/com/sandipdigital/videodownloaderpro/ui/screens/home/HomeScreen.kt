package com.sandipdigital.videodownloaderpro.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.data.model.MediaKind
import com.sandipdigital.videodownloaderpro.util.FileUtils

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onDownloadStarted: () -> Unit
) {
    val url by viewModel.url.collectAsState()
    val fetchState by viewModel.fetchState.collectAsState()
    val downloadStarted by viewModel.downloadStarted.collectAsState()
    var extractAudio by remember { mutableStateOf(false) }

    LaunchedEffect(downloadStarted) {
        if (downloadStarted) {
            onDownloadStarted()
            viewModel.consumeDownloadStartedFlag()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Video Downloader Pro", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Paste any direct HTTPS video or audio link to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = url,
            onValueChange = viewModel::onUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Direct link") },
            placeholder = { Text("https://example.com/video.mp4") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = viewModel::fetchInfo,
            enabled = url.isNotBlank() && fetchState !is FetchState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (fetchState is FetchState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Fetch Info")
            }
        }

        AnimatedVisibility(
            visible = fetchState is FetchState.Error,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val message = (fetchState as? FetchState.Error)?.message.orEmpty()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        AnimatedVisibility(
            visible = fetchState is FetchState.Success,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val info = (fetchState as? FetchState.Success)?.info
            if (info != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (info.kind == MediaKind.AUDIO) Icons.Default.AudioFile else Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(info.fileName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Type: ${info.mimeType}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Size: ${if (info.sizeBytes > 0) FileUtils.humanReadableBytes(info.sizeBytes) else "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Resumable: ${if (info.supportsResume) "Yes" else "No"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (info.kind == MediaKind.VIDEO) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = extractAudio, onCheckedChange = { extractAudio = it })
                                Text("Also extract audio track (.m4a) after download")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.startDownload(extractAudio) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Start Download")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Only download content you own or have the right to download. This app works with direct links you provide — it does not bypass any platform's protections.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
