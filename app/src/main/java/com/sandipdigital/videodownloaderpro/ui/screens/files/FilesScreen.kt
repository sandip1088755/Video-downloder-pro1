package com.sandipdigital.videodownloaderpro.ui.screens.files

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
import com.sandipdigital.videodownloaderpro.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onOpenPlayer: (String, Boolean) -> Unit,
    viewModel: FilesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var renameTarget by remember { mutableStateOf<FileEntry?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("File Manager", fontWeight = FontWeight.SemiBold) }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Storage analyzer summary
            ElevatedCard(Modifier.padding(16.dp).fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Storage used", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            FileUtils.humanReadableSize(state.totalUsedBytes),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    OutlinedButton(onClick = { viewModel.scanForDuplicates() }, enabled = !state.isScanning) {
                        Icon(Icons.Filled.FindReplace, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.isScanning) "Scanning…" else "Find duplicates")
                    }
                }
            }

            if (state.duplicateGroups.isNotEmpty()) {
                Card(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        "${state.duplicateGroups.size} duplicate group(s) found — review in the list below.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChanged,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                placeholder = { Text("Search downloaded files") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            val entries = viewModel.filteredEntries()
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No files yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.file.absolutePath }) { entry ->
                        FileRow(
                            entry = entry,
                            onOpen = { onOpenPlayer(entry.file.absolutePath, !entry.isVideo) },
                            onRename = { renameTarget = entry },
                            onDelete = { viewModel.delete(entry) },
                            onShare = { /* Intent.ACTION_SEND via FileProvider — wired at Activity level */ }
                        )
                    }
                }
            }
        }
    }

    renameTarget?.let { entry ->
        var newName by remember(entry) { mutableStateOf(entry.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename file") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(entry, newName)
                    renameTarget = null
                }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun FileRow(
    entry: FileEntry,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (entry.isVideo) Icons.Filled.Videocam else Icons.Filled.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.name, maxLines = 1, style = MaterialTheme.typography.bodyLarge)
                Text(
                    FileUtils.humanReadableSize(entry.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpen) { Icon(Icons.Filled.PlayArrow, contentDescription = "Play") }
            IconButton(onClick = onShare) { Icon(Icons.Filled.Share, contentDescription = "Share") }
            IconButton(onClick = onRename) { Icon(Icons.Filled.Edit, contentDescription = "Rename") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        }
    }
}
