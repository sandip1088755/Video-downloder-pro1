package com.sandipdigital.videodownloaderpro.ui.screens.home

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.data.datastore.SettingsDataStore
import com.sandipdigital.videodownloaderpro.data.network.UrlCheckResult
import com.sandipdigital.videodownloaderpro.data.network.UrlValidator
import com.sandipdigital.videodownloaderpro.data.repository.DownloadRepository
import com.sandipdigital.videodownloaderpro.domain.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class HomeUiState(
    val urlInput: String = "",
    val isChecking: Boolean = false,
    val detected: UrlCheckResult.Valid? = null,
    val errorMessage: String? = null,
    val justEnqueued: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val urlValidator: UrlValidator,
    private val repository: DownloadRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onUrlChanged(newUrl: String) {
        _uiState.value = _uiState.value.copy(urlInput = newUrl, detected = null, errorMessage = null)
    }

    fun detectMedia() {
        val url = _uiState.value.urlInput.trim()
        if (url.isEmpty()) return

        _uiState.value = _uiState.value.copy(isChecking = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = urlValidator.probe(url)) {
                is UrlCheckResult.Valid -> _uiState.value = _uiState.value.copy(isChecking = false, detected = result)
                is UrlCheckResult.Invalid -> _uiState.value =
                    _uiState.value.copy(isChecking = false, errorMessage = result.reason)
            }
        }
    }

    fun confirmDownload(mediaType: MediaType) {
        val detected = _uiState.value.detected ?: return
        viewModelScope.launch {
            val settings = settingsDataStore.settingsFlow.first()

            // Duplicate detection before we spend any bandwidth.
            val duplicate = repository.findDuplicate(detected.suggestedFileName, detected.contentLengthBytes)
            if (duplicate != null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "This file already exists in your downloads: ${duplicate.title}"
                )
                return@launch
            }

            val destDir = resolveDestinationDir(settings.storageLocation)
            repository.enqueue(
                url = detected.url,
                title = detected.suggestedFileName,
                mediaType = mediaType,
                destinationDir = destDir,
                fileName = detected.suggestedFileName,
                maxParallel = settings.maxParallelDownloads,
                wifiOnly = settings.wifiOnly
            )

            _uiState.value = HomeUiState(justEnqueued = true)
        }
    }

    private fun resolveDestinationDir(customPath: String): File {
        if (customPath.isNotBlank()) {
            val custom = File(customPath)
            if (custom.exists() || custom.mkdirs()) return custom
        }
        // Default: app-specific external directory — no extra storage permission
        // needed on modern Android, and it's automatically cleaned up on uninstall.
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
