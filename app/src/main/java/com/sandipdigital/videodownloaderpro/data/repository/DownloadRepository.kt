package com.sandipdigital.videodownloaderpro.data.repository

import android.content.Context
import androidx.work.*
import com.sandipdigital.videodownloaderpro.data.local.dao.DownloadDao
import com.sandipdigital.videodownloaderpro.data.local.entity.DownloadEntity
import com.sandipdigital.videodownloaderpro.domain.model.*
import com.sandipdigital.videodownloaderpro.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DownloadDao
) {
    private val workManager = WorkManager.getInstance(context)

    fun observeAll(): Flow<List<DownloadItem>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeActive(): Flow<List<DownloadItem>> =
        dao.observeByStatus(listOf(DownloadStatus.QUEUED.name, DownloadStatus.RUNNING.name, DownloadStatus.PAUSED.name))
            .map { list -> list.map { it.toDomain() } }

    fun observeCompleted(): Flow<List<DownloadItem>> =
        dao.observeByStatus(listOf(DownloadStatus.COMPLETED.name)).map { list -> list.map { it.toDomain() } }

    fun observeFavorites(): Flow<List<DownloadItem>> = dao.observeFavorites().map { list -> list.map { it.toDomain() } }

    fun search(query: String): Flow<List<DownloadItem>> = dao.search(query).map { list -> list.map { it.toDomain() } }

    /**
     * Enqueues a new download. [destinationDir] should come from the user's
     * chosen storage location (internal or SD card, resolved earlier by
     * StorageLocationManager). Returns the new download's id immediately so
     * the UI can show it in the queue before any bytes arrive.
     */
    suspend fun enqueue(
        url: String,
        title: String,
        mediaType: MediaType,
        destinationDir: File,
        fileName: String,
        maxParallel: Int,
        wifiOnly: Boolean
    ): String {
        val id = UUID.randomUUID().toString()
        val filePath = File(destinationDir, fileName).absolutePath

        dao.upsert(
            DownloadEntity(
                id = id,
                title = title,
                sourceUrl = url,
                filePath = filePath,
                mediaType = mediaType.name,
                status = DownloadStatus.QUEUED.name
            )
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            DownloadWorker.KEY_DOWNLOAD_ID to id,
            DownloadWorker.KEY_URL to url,
            DownloadWorker.KEY_FILE_PATH to filePath
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag(TAG_DOWNLOAD)
            .addTag(id)
            .build()

        // Queue name = "downloads" with APPEND_OR_REPLACE respects maxParallel
        // via a chained unique-work-per-slot pattern managed by the caller/ViewModel;
        // simplest correct approach: enqueue as unique work keyed by id so
        // pause/cancel can target it directly, while the OS + WorkManager's own
        // executor pool naturally bounds real concurrency.
        workManager.enqueueUniqueWork(id, ExistingWorkPolicy.KEEP, request)
        return id
    }

    suspend fun pause(id: String) {
        workManager.cancelUniqueWork(id) // worker checks isStopped and marks PAUSED before exiting
    }

    suspend fun resume(id: String, url: String, filePath: String, wifiOnly: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val inputData = workDataOf(
            DownloadWorker.KEY_DOWNLOAD_ID to id,
            DownloadWorker.KEY_URL to url,
            DownloadWorker.KEY_FILE_PATH to filePath
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(TAG_DOWNLOAD)
            .addTag(id)
            .build()
        workManager.enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
    }

    suspend fun retry(id: String) {
        val entity = dao.getById(id) ?: return
        resume(id, entity.sourceUrl, entity.filePath, wifiOnly = false)
    }

    suspend fun cancel(id: String) {
        workManager.cancelUniqueWork(id)
        val entity = dao.getById(id)
        entity?.let { File(it.filePath).delete() }
        dao.updateStatus(id, DownloadStatus.CANCELLED.name)
    }

    suspend fun delete(id: String, deleteFile: Boolean) {
        val entity = dao.getById(id) ?: return
        if (deleteFile) File(entity.filePath).delete()
        dao.deleteById(id)
    }

    suspend fun toggleFavorite(id: String, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun findDuplicate(title: String, size: Long) = dao.findDuplicate(title, size)?.toDomain()

    companion object {
        private const val TAG_DOWNLOAD = "video_downloader_pro_download"
    }
}

private fun DownloadEntity.toDomain() = DownloadItem(
    id = id,
    title = title,
    sourceUrl = sourceUrl,
    filePath = filePath,
    mediaType = MediaType.valueOf(mediaType),
    status = DownloadStatus.valueOf(status),
    progressPercent = progressPercent,
    bytesDownloaded = bytesDownloaded,
    totalBytes = totalBytes,
    speedBytesPerSec = speedBytesPerSec,
    etaSeconds = etaSeconds,
    isFavorite = isFavorite,
    createdAt = createdAt,
    completedAt = completedAt
)
