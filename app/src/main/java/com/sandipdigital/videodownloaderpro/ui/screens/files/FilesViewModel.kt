package com.sandipdigital.videodownloaderpro.ui.screens.files

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class FileEntry(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isVideo: Boolean
)

data class FilesUiState(
    val entries: List<FileEntry> = emptyList(),
    val searchQuery: String = "",
    val totalUsedBytes: Long = 0L,
    val duplicateGroups: List<List<File>> = emptyList(),
    val isScanning: Boolean = false
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val downloadsDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = downloadsDir.listFiles()?.filter { it.isFile } ?: emptyList()
            val entries = files.map {
                FileEntry(
                    file = it,
                    name = it.name,
                    sizeBytes = it.length(),
                    lastModified = it.lastModified(),
                    isVideo = it.extension.lowercase() in setOf("mp4", "mkv", "webm", "mov", "avi", "3gp")
                )
            }.sortedByDescending { it.lastModified }

            _uiState.value = _uiState.value.copy(
                entries = entries,
                totalUsedBytes = FileUtils.folderSizeBytes(downloadsDir)
            )
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun filteredEntries(): List<FileEntry> {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return _uiState.value.entries
        return _uiState.value.entries.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun rename(entry: FileEntry, newName: String) = viewModelScope.launch(Dispatchers.IO) {
        FileUtils.renameFile(entry.file, newName)
        refresh()
    }

    fun delete(entry: FileEntry) = viewModelScope.launch(Dispatchers.IO) {
        FileUtils.deleteFile(entry.file)
        refresh()
    }

    fun move(entry: FileEntry, destination: File) = viewModelScope.launch(Dispatchers.IO) {
        FileUtils.moveFile(entry.file, destination)
        refresh()
    }

    fun scanForDuplicates() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value = _uiState.value.copy(isScanning = true)
        val groups = FileUtils.findDuplicates(_uiState.value.entries.map { it.file })
        _uiState.value = _uiState.value.copy(isScanning = false, duplicateGroups = groups)
    }
}
