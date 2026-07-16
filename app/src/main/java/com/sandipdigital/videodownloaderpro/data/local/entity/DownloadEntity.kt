package com.sandipdigital.videodownloaderpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single source of truth for a download's persisted state. WorkManager
 * updates this row as bytes stream in, and the UI observes it via Flow.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val sourceUrl: String,
    val filePath: String,
    val mediaType: String,          // MediaType enum name
    val status: String,             // DownloadStatus enum name
    val progressPercent: Int = 0,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val isFavorite: Boolean = false,
    val threadCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)
