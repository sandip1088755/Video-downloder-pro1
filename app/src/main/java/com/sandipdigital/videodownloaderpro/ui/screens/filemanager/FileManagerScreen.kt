package com.sandipdigital.videodownloaderpro.ui.screens.filemanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.util.FileUtils
import java.io.File

@Composable
fun FileManagerScreen(viewModel: FileManagerViewModel = hiltViewModel()) {
    val files by viewModel.files.collectAsState()
    val context = LocalContext.current
    var renameTarget by remember { mutableStateOf<File?>(null) }
    var renameText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Files", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Refresh")
            }
        }
        Spacer(Modifier.height(16.dp))

        if (files.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files downloaded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(files, key = { it.absolutePath }) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        onClick = { context.startActivity(FileUtils.openIntent(context, file)) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(file.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                FileUtils.humanReadableBytes(file.length()),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    renameTarget = file
                                    renameText = file.nameWithoutExtension
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp)); Text("Rename")
                                }
                                OutlinedButton(onClick = {
                                    context.startActivity(
                                        android.content.Intent.createChooser(FileUtils.shareIntent(context, file), "Share")
                                    )
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp)); Text("Share")
                                }
                                OutlinedButton(onClick = { viewModel.delete(file) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp)); Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val target = renameTarget
    if (target != null) {
        val ext = target.extension
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename file") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    suffix = { if (ext.isNotBlank()) Text(".$ext") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newName = if (ext.isNotBlank()) "$renameText.$ext" else renameText
                    viewModel.rename(target, newName)
                    renameTarget = null
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }
}
