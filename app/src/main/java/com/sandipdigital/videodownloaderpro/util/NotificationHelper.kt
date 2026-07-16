package com.sandipdigital.videodownloaderpro.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sandipdigital.videodownloaderpro.R

object NotificationHelper {
    const val CHANNEL_DOWNLOADS = "downloads_channel"
    const val CHANNEL_COMPLETE = "downloads_complete_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val progressChannel = NotificationChannel(
                CHANNEL_DOWNLOADS,
                "Active downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows progress of ongoing downloads" }

            val completeChannel = NotificationChannel(
                CHANNEL_COMPLETE,
                "Completed downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifies when a download finishes" }

            manager.createNotificationChannel(progressChannel)
            manager.createNotificationChannel(completeChannel)
        }
    }

    fun buildProgressNotification(
        context: Context,
        title: String,
        progress: Int,
        speedLabel: String,
        etaLabel: String
    ): androidx.core.app.NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setContentTitle(title)
            .setContentText("$progress% • $speedLabel • ETA $etaLabel")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun buildCompleteNotification(context: Context, title: String, success: Boolean): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_COMPLETE)
            .setContentTitle(if (success) "Download complete" else "Download failed")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}
