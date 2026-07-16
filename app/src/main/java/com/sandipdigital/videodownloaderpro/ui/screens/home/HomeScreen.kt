package com.sandipdigital.videodownloaderpro.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPlayer: (String, Boolean) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Video Downloader Pro", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Paste a direct video or audio link to download it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.urlInput,
                onValueChange = viewModel::onUrlChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Media URL") },
                placeholder = { Text("https://example.com/video.mp4") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.getText()?.text?.let { viewModel.onUrlChanged(it) }
                    }) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Paste from clipboard")
                    }
                }
            )

            Button(
                onClick = viewModel::detectMedia,
                enabled = state.urlInput.isNotBlank() && !state.isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isChecking) "Checking…" else "Detect Media")
            }

            state.errorMessage?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            AnimatedVisibility(visible = state.detected != null) {
                state.detected?.let { detected ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(detected.suggestedFileName, style = MaterialTheme.typography.titleMedium)
                            val sizeText = if (detected.contentLengthBytes > 0) {
                                "%.1f MB".format(detected.contentLengthBytes / (1024.0 * 1024.0))
                            } else "Unknown size"
                            Text(
                                "$sizeText · ${detected.contentType ?: "media"}" +
                                    (if (detected.supportsRangeRequests) " · resumable" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { viewModel.confirmDownload(MediaType.VIDEO) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Video")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.confirmDownload(MediaType.AUDIO) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Audio only")
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.justEnqueued) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Added to your download queue. Check the Downloads tab for progress.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
