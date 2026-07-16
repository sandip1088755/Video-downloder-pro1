package com.sandipdigital.videodownloaderpro.util

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    fun downloadsDir(context: Context): File {
        val dir = context.getExternalFilesDir("Downloads") ?: File(context.filesDir, "downloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
        return cleaned.ifBlank { "file_${System.currentTimeMillis()}" }
    }

    fun uniqueFile(dir: File, desiredName: String): File {
        val sanitized = sanitizeFileName(desiredName)
        var candidate = File(dir, sanitized)
        if (!candidate.exists()) return candidate
        val base = sanitized.substringBeforeLast('.', sanitized)
        val ext = sanitized.substringAfterLast('.', "")
        var counter = 1
        while (candidate.exists()) {
            val newName = if (ext.isNotBlank()) "${base}_$counter.$ext" else "${base}_$counter"
            candidate = File(dir, newName)
            counter++
        }
        return candidate
    }

    fun humanReadableBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
    }

    fun humanReadableSpeed(bytesPerSecond: Long): String =
        "${humanReadableBytes(bytesPerSecond)}/s"

    fun formatEta(seconds: Long): String {
        if (seconds < 0) return "--"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return when {
            h > 0 -> String.format(Locale.getDefault(), "%dh %02dm", h, m)
            m > 0 -> String.format(Locale.getDefault(), "%dm %02ds", m, s)
            else -> String.format(Locale.getDefault(), "%ds", s)
        }
    }

    fun mimeTypeForFile(file: File): String {
        val ext = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    }

    fun contentUriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun shareIntent(context: Context, file: File): Intent {
        val uri = contentUriFor(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeTypeForFile(file)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun openIntent(context: Context, file: File): Intent {
        val uri = contentUriFor(context, file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeTypeForFile(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun renameFile(file: File, newName: String): File? {
        val target = File(file.parentFile, sanitizeFileName(newName))
        return if (file.renameTo(target)) target else null
    }

    fun deleteFile(file: File): Boolean = file.exists() && file.delete()
}
