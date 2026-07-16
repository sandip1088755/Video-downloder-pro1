package com.sandipdigital.videodownloaderpro.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.data.local.DownloadEntity
import com.sandipdigital.videodownloaderpro.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    val activeDownloads: StateFlow<List<DownloadEntity>> = repository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun pause(id: Long) = viewModelScope.launch { repository.pause(id) }
    fun resume(id: Long) = viewModelScope.launch { repository.resume(id) }
    fun cancel(id: Long) = viewModelScope.launch { repository.cancel(id) }
    fun retry(id: Long) = viewModelScope.launch { repository.retry(id) }
}
