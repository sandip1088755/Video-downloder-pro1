package com.sandipdigital.videodownloaderpro.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.sandipdigital.videodownloaderpro.data.local.dao.DownloadDao
import com.sandipdigital.videodownloaderpro.domain.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.max

/**
 * Background download engine backed by WorkManager, so downloads survive
 * process death, doze mode, and app swipe-away. Supports:
 *  - Resume via HTTP Range requests (if the server advertises Accept-Ranges).
 *  - Cancellation (WorkManager cancellation cooperatively checked each chunk).
 *  - Retry with WorkManager's built-in backoff policy (configured at enqueue time).
 *  - Speed + ETA calculation surfaced to Room -> UI in real time.
 *
 * One worker instance = one download task. "Multi-thread" downloading for a
 * single file is achieved by splitting the byte range across N segment
 * workers dispatched from [MultiThreadDownloadCoordinator]; this class is the
 * single-segment/single-file primitive both paths share.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val httpClient: OkHttpClient,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_URL = "url"
        const val KEY_FILE_PATH = "file_path"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val id = inputData.getString(KEY_DOWNLOAD_ID) ?: "download"
        return notificationHelper.buildProgressForegroundInfo(id, "Downloading…", 0)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return@withContext Result.failure()
        val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return@withContext Result.failure()

        setForeground(getForegroundInfo())
        downloadDao.updateStatus(downloadId, DownloadStatus.RUNNING.name)

        val file = File(filePath)
        file.parentFile?.mkdirs()
        val alreadyDownloaded = if (file.exists()) file.length() else 0L

        return@withContext try {
            val requestBuilder = Request.Builder().url(url)
            if (alreadyDownloaded > 0) {
                // Resume: ask the server for bytes past what we already have.
                requestBuilder.header("Range", "bytes=$alreadyDownloaded-")
            }

            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name, "HTTP ${response.code}")
                    return@withContext Result.retry()
                }

                val body = response.body ?: run {
                    downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name, "Empty response body")
                    return@withContext Result.failure()
                }

                val isResumedRange = response.code == 206
                val totalBytes = if (isResumedRange) {
                    alreadyDownloaded + body.contentLength()
                } else {
                    body.contentLength()
                }

                RandomAccessFile(file, "rw").use { raf ->
                    val startPos = if (isResumedRange) alreadyDownloaded else 0L
                    raf.seek(startPos)

                    body.byteStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        var totalRead = startPos
                        var lastUpdateTime = System.currentTimeMillis()
                        var bytesSinceLastUpdate = 0L

                        while (true) {
                            // Cooperative cancellation check for Pause/Cancel actions.
                            if (isStopped) {
                                downloadDao.updateStatus(downloadId, DownloadStatus.PAUSED.name)
                                return@withContext Result.success()
                            }

                            val read = input.read(buffer)
                            if (read == -1) break

                            raf.write(buffer, 0, read)
                            totalRead += read
                            bytesSinceLastUpdate += read

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastUpdateTime
                            if (elapsed >= PROGRESS_UPDATE_INTERVAL_MS) {
                                val speed = (bytesSinceLastUpdate * 1000L) / max(elapsed, 1L)
                                val remaining = (totalBytes - totalRead).coerceAtLeast(0)
                                val eta = if (speed > 0) remaining / speed else -1L
                                val progress = if (totalBytes > 0) {
                                    ((totalRead * 100) / totalBytes).toInt()
                                } else 0

                                downloadDao.updateProgress(
                                    id = downloadId,
                                    status = DownloadStatus.RUNNING.name,
                                    progress = progress,
                                    bytes = totalRead,
                                    total = totalBytes,
                                    speed = speed,
                                    eta = eta
                                )
                                notificationHelper.updateProgressNotification(downloadId, progress)

                                lastUpdateTime = now
                                bytesSinceLastUpdate = 0L
                            }
                        }
                    }
                }

                downloadDao.markCompleted(downloadId, System.currentTimeMillis())
                notificationHelper.showCompletedNotification(downloadId, file.name)
                Result.success()
            }
        } catch (e: Exception) {
            downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name, e.localizedMessage)
            // Let WorkManager's backoff policy decide the retry cadence.
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
