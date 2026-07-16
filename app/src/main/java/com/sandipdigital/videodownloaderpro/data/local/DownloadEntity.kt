package com.sandipdigital.videodownloaderpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sandipdigital.videodownloaderpro.data.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.data.model.MediaKind

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceUrl: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val totalBytes: Long,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val kind: MediaKind = MediaKind.OTHER,
    val extractAudio: Boolean = false,
    val workRequestId: String? = null,
    val speedBps: Long = 0,
    val etaSeconds: Long = -1,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
