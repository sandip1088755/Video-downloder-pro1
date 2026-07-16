package com.sandipdigital.videodownloaderpro.ui.screens.filemanager

import android.content.Context
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

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val dir = FileUtils.downloadsDir(context)
            _files.value = withContext(Dispatchers.IO) {
                dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
            }
        }
    }

    fun rename(file: File, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FileUtils.renameFile(file, newName)
            refresh()
        }
    }

    fun delete(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            FileUtils.deleteFile(file)
            refresh()
        }
    }
}
