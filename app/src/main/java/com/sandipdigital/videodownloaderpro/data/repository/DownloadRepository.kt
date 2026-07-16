package com.sandipdigital.videodownloaderpro.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sandipdigital.videodownloaderpro.data.local.DownloadDao
import com.sandipdigital.videodownloaderpro.data.local.DownloadEntity
import com.sandipdigital.videodownloaderpro.data.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.data.model.MediaKind
import com.sandipdigital.videodownloaderpro.data.model.RemoteFileInfo
import com.sandipdigital.videodownloaderpro.util.FileUtils
import com.sandipdigital.videodownloaderpro.util.PreferencesManager
import com.sandipdigital.videodownloaderpro.util.UrlValidator
import com.sandipdigital.videodownloaderpro.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DownloadDao,
    private val okHttpClient: OkHttpClient,
    private val preferencesManager: PreferencesManager,
    private val workManager: WorkManager
) {

    fun observeAll(): Flow<List<DownloadEntity>> = dao.observeAll()

    fun observeActive(): Flow<List<DownloadEntity>> =
        dao.observeByStatus(listOf(DownloadStatus.QUEUED, DownloadStatus.RUNNING, DownloadStatus.PAUSED, DownloadStatus.FAILED))

    fun observeHistory(): Flow<List<DownloadEntity>> =
        dao.observeByStatusSingle(DownloadStatus.COMPLETED)

    fun search(query: String): Flow<List<DownloadEntity>> = dao.search(query)

    /**
     * Performs a lightweight HEAD (falling back to a ranged GET) request to discover
     * file name, size, and MIME type for a direct HTTPS link before downloading.
     */
    suspend fun fetchFileInfo(rawUrl: String): Result<RemoteFileInfo> = withContext(Dispatchers.IO) {
        val url = rawUrl.trim()
        if (!UrlValidator.isValidHttpsUrl(url)) {
            return@withContext Result.failure(IllegalArgumentException("Only secure HTTPS direct links are supported"))
        }
        try {
            var request = Request.Builder().url(url).head().build()
            var response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful || response.header("Content-Length") == null) {
                response.close()
                // Some servers don't support HEAD — fall back to a ranged GET
                request = Request.Builder().url(url).header("Range", "bytes=0-0").get().build()
                response = okHttpClient.newCall(request).execute()
            }

            response.use { resp ->
                if (!resp.isSuccessful && resp.code != 206) {
                    return@withContext Result.failure(Exception("Server responded with ${resp.code}"))
                }
                val contentType = resp.header("Content-Type")?.substringBefore(";") ?: "application/octet-stream"
                val acceptRanges = resp.header("Accept-Ranges")
                val contentRange = resp.header("Content-Range")
                val totalSize = contentRange?.substringAfterLast('/')?.toLongOrNull()
                    ?: resp.header("Content-Length")?.toLongOrNull() ?: -1L
                val supportsResume = acceptRanges == "bytes" || contentRange != null

                val dispositionName = resp.header("Content-Disposition")
                    ?.substringAfter("filename=", "")
                    ?.trim('"', ' ')
                    ?.takeIf { it.isNotBlank() }

                val fileName = FileUtils.sanitizeFileName(dispositionName ?: UrlValidator.guessFileName(url))
                val ext = UrlValidator.extensionOf(fileName)
                val kind = when {
                    UrlValidator.isVideoExtension(ext) || contentType.startsWith("video") -> MediaKind.VIDEO
                    UrlValidator.isAudioExtension(ext) || contentType.startsWith("audio") -> MediaKind.AUDIO
                    else -> MediaKind.OTHER
                }

                Result.success(
                    RemoteFileInfo(
                        url = url,
                        fileName = fileName,
                        mimeType = contentType,
                        sizeBytes = totalSize,
                        supportsResume = supportsResume,
                        kind = kind
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enqueueDownload(info: RemoteFileInfo, extractAudio: Boolean = false): Long = withContext(Dispatchers.IO) {
        val targetDir = FileUtils.downloadsDir(context)
        val destFile = FileUtils.uniqueFile(targetDir, info.fileName)

        val entity = DownloadEntity(
            sourceUrl = info.url,
            fileName = destFile.name,
            filePath = destFile.absolutePath,
            mimeType = info.mimeType,
            totalBytes = info.sizeBytes,
            status = DownloadStatus.QUEUED,
            kind = info.kind,
            extractAudio = extractAudio
        )
        val id = dao.insert(entity)
        startWork(id)
        id
    }

    suspend fun pause(id: Long) = withContext(Dispatchers.IO) {
        workManager.cancelUniqueWork(workName(id))
        dao.getById(id)?.let { dao.update(it.copy(status = DownloadStatus.PAUSED, updatedAt = System.currentTimeMillis())) }
    }

    suspend fun resume(id: Long) = withContext(Dispatchers.IO) {
        dao.getById(id)?.let {
            dao.update(it.copy(status = DownloadStatus.QUEUED, errorMessage = null, updatedAt = System.currentTimeMillis()))
        }
        startWork(id)
    }

    suspend fun retry(id: Long) = resume(id)

    suspend fun cancel(id: Long) = withContext(Dispatchers.IO) {
        workManager.cancelUniqueWork(workName(id))
        dao.getById(id)?.let { entity ->
            java.io.File(entity.filePath).takeIf { it.exists() }?.delete()
            dao.update(entity.copy(status = DownloadStatus.CANCELLED, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecord(id: Long, deleteFile: Boolean) = withContext(Dispatchers.IO) {
        dao.getById(id)?.let { entity ->
            if (deleteFile) java.io.File(entity.filePath).takeIf { it.exists() }?.delete()
            dao.delete(entity)
        }
    }

    fun observeWorkInfo(id: Long): Flow<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkFlow(workName(id))

    private suspend fun startWork(id: Long) {
        val wifiOnly = preferencesManager.wifiOnly.first()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(Data.Builder().putLong(DownloadWorker.KEY_DOWNLOAD_ID, id).build())
            .setConstraints(constraints)
            .setBackoffCriteria(androidx.work.BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        dao.getById(id)?.let { dao.update(it.copy(workRequestId = request.id.toString())) }
        workManager.enqueueUniqueWork(workName(id), ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(id: Long) = "download_$id"
}
