package com.sandipdigital.videodownloaderpro.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.data.datastore.SettingsDataStore
import com.sandipdigital.videodownloaderpro.data.repository.DownloadRepository
import com.sandipdigital.videodownloaderpro.domain.model.DownloadItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val activeDownloads: StateFlow<List<DownloadItem>> = repository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadItem>> = repository.observeCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun pause(id: String) = viewModelScope.launch { repository.pause(id) }

    fun resume(item: DownloadItem) = viewModelScope.launch {
        val wifiOnly = settingsDataStore.settingsFlow.first().wifiOnly
        repository.resume(item.id, item.sourceUrl, item.filePath, wifiOnly = wifiOnly)
    }

    fun retry(id: String) = viewModelScope.launch { repository.retry(id) }

    fun cancel(id: String) = viewModelScope.launch { repository.cancel(id) }

    fun delete(id: String, deleteFile: Boolean) = viewModelScope.launch { repository.delete(id, deleteFile) }

    fun toggleFavorite(id: String, favorite: Boolean) = viewModelScope.launch {
        repository.toggleFavorite(id, favorite)
    }
}
