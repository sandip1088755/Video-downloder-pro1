package com.sandipdigital.videodownloaderpro.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.sandipdigital.videodownloaderpro.data.local.DownloadDao
import com.sandipdigital.videodownloaderpro.data.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.util.NotificationHelper
import com.sandipdigital.videodownloaderpro.util.FileUtils
import com.sandipdigital.videodownloaderpro.util.SpeedEtaCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: DownloadDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        private const val MAX_RETRIES = 5
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
        private const val NOTIFICATION_ID_BASE = 5000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val id = inputData.getLong(KEY_DOWNLOAD_ID, -1L)
        if (id == -1L) return@withContext Result.failure()

        val entity = dao.getById(id) ?: return@withContext Result.failure()
        val destFile = File(entity.filePath)
        destFile.parentFile?.mkdirs()

        dao.update(entity.copy(status = DownloadStatus.RUNNING, updatedAt = System.currentTimeMillis()))
        setForegroundAsync(makeForegroundInfo(id.toInt(), entity.fileName, 0))

        val existingBytes = if (destFile.exists()) destFile.length() else 0L

        return@withContext try {
            val requestBuilder = Request.Builder().url(entity.sourceUrl)
            if (existingBytes > 0) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
            }
            val response = okHttpClient.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful && response.code != 206) {
                response.close()
                return@withContext failOrRetry(entity.id, entity.fileName, "Server error ${response.code}")
            }

            val body = response.body ?: return@withContext failOrRetry(entity.id, entity.fileName, "Empty response body")
            val isResuming = response.code == 206 && existingBytes > 0
            val totalBytes = if (isResuming) entity.totalBytes else (body.contentLength().takeIf { it > 0 } ?: entity.totalBytes)

            val raf = RandomAccessFile(destFile, "rw")
            if (isResuming) raf.seek(existingBytes) else raf.setLength(0)

            val calculator = SpeedEtaCalculator()
            var downloaded = if (isResuming) existingBytes else 0L
            var lastUpdateMs = System.currentTimeMillis()

            body.byteStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    if (isStopped) {
                        raf.close()
                        response.close()
                        dao.getById(entity.id)?.let {
                            dao.update(it.copy(status = DownloadStatus.PAUSED, downloadedBytes = downloaded, updatedAt = System.currentTimeMillis()))
                        }
                        return@withContext Result.success()
                    }

                    val read = input.read(buffer)
                    if (read == -1) break
                    raf.write(buffer, 0, read)
                    downloaded += read

                    val now = System.currentTimeMillis()
                    if (now - lastUpdateMs >= PROGRESS_UPDATE_INTERVAL_MS) {
                        calculator.addSample(now, downloaded)
                        val speed = calculator.currentSpeedBps()
                        val eta = calculator.etaSeconds(totalBytes, downloaded)
                        val progressPercent = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0

                        dao.getById(entity.id)?.let {
                            dao.update(
                                it.copy(
                                    downloadedBytes = downloaded,
                                    totalBytes = if (totalBytes > 0) totalBytes else it.totalBytes,
                                    speedBps = speed,
                                    etaSeconds = eta,
                                    updatedAt = now
                                )
                            )
                        }
                        setForegroundAsync(makeForegroundInfo(id.toInt(), entity.fileName, progressPercent, speed, eta))
                        lastUpdateMs = now
                    }
                }
            }
            raf.close()
            response.close()

            dao.getById(entity.id)?.let {
                dao.update(
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = downloaded,
                        totalBytes = if (totalBytes > 0) totalBytes else downloaded,
                        speedBps = 0,
                        etaSeconds = 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            if (entity.extractAudio) {
                com.sandipdigital.videodownloaderpro.util.AudioExtractor.extractAudioTrack(destFile)
            }
            notifyComplete(id.toInt(), entity.fileName, true)
            Result.success()
        } catch (e: Exception) {
            failOrRetry(entity.id, entity.fileName, e.message ?: "Download error")
        }
    }

    private suspend fun failOrRetry(id: Long, fileName: String, errorMessage: String): Result {
        val attemptsSoFar = runAttemptCount
        return if (attemptsSoFar < MAX_RETRIES) {
            dao.getById(id)?.let {
                dao.update(it.copy(status = DownloadStatus.QUEUED, errorMessage = errorMessage, updatedAt = System.currentTimeMillis()))
            }
            Result.retry()
        } else {
            dao.getById(id)?.let {
                dao.update(it.copy(status = DownloadStatus.FAILED, errorMessage = errorMessage, updatedAt = System.currentTimeMillis()))
            }
            notifyComplete(id.toInt(), fileName, false)
            Result.failure()
        }
    }

    private fun notifyComplete(id: Int, fileName: String, success: Boolean) {
        val notification = NotificationHelper.buildCompleteNotification(applicationContext, fileName, success).build()
        NotificationManagerCompat.from(applicationContext).apply {
            notify(NOTIFICATION_ID_BASE + id, notification)
        }
    }

    private fun makeForegroundInfo(id: Int, fileName: String, progress: Int, speedBps: Long = 0, etaSeconds: Long = -1): ForegroundInfo {
        val notification = NotificationHelper.buildProgressNotification(
            applicationContext, fileName, progress,
            FileUtils.humanReadableSpeed(speedBps),
            FileUtils.formatEta(etaSeconds)
        ).build()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID_BASE + id, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID_BASE + id, notification)
        }
    }
}
