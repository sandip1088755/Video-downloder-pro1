package com.sandipdigital.videodownloaderpro.ui.screens.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.sandipdigital.videodownloaderpro.data.local.DownloadDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DownloadDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    init {
        val downloadId = savedStateHandle.get<String>("downloadId")?.toLongOrNull()
        if (downloadId != null) {
            viewModelScope.launch {
                dao.getById(downloadId)?.let { entity ->
                    _title.value = entity.fileName
                    exoPlayer.setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(java.io.File(entity.filePath))))
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }
            }
        }
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
