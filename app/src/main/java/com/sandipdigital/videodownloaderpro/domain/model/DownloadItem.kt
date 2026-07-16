package com.sandipdigital.videodownloaderpro.domain.model

/**
 * UI-facing, immutable representation of a download. Mapped from
 * [com.sandipdigital.videodownloaderpro.data.local.entity.DownloadEntity].
 */
data class DownloadItem(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val filePath: String,
    val mediaType: MediaType,
    val status: DownloadStatus,
    val progressPercent: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val etaSeconds: Long,
    val isFavorite: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)
