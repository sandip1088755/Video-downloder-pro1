package com.sandipdigital.videodownloaderpro.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.data.model.RemoteFileInfo
import com.sandipdigital.videodownloaderpro.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FetchState {
    object Idle : FetchState()
    object Loading : FetchState()
    data class Success(val info: RemoteFileInfo) : FetchState()
    data class Error(val message: String) : FetchState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    private val _url = MutableStateFlow(com.sandipdigital.videodownloaderpro.util.IncomingLinkHolder.consume().orEmpty())
    val url: StateFlow<String> = _url.asStateFlow()

    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Idle)
    val fetchState: StateFlow<FetchState> = _fetchState.asStateFlow()

    private val _downloadStarted = MutableStateFlow(false)
    val downloadStarted: StateFlow<Boolean> = _downloadStarted.asStateFlow()

    fun onUrlChanged(value: String) {
        _url.value = value
        _fetchState.value = FetchState.Idle
    }

    fun fetchInfo() {
        val current = _url.value
        viewModelScope.launch {
            _fetchState.value = FetchState.Loading
            repository.fetchFileInfo(current)
                .onSuccess { _fetchState.value = FetchState.Success(it) }
                .onFailure { _fetchState.value = FetchState.Error(it.message ?: "Could not read this link") }
        }
    }

    fun startDownload(extractAudio: Boolean) {
        val state = _fetchState.value
        if (state !is FetchState.Success) return
        viewModelScope.launch {
            repository.enqueueDownload(state.info, extractAudio)
            _downloadStarted.value = true
            _url.value = ""
            _fetchState.value = FetchState.Idle
        }
    }

    fun consumeDownloadStartedFlag() {
        _downloadStarted.value = false
    }
}
