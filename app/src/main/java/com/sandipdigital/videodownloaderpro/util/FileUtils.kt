package com.sandipdigital.videodownloaderpro.util

import java.io.File
import java.security.MessageDigest

/**
 * Local file-system helpers used by the File Manager screen: human-readable
 * sizes, storage-usage rollups, and a lightweight duplicate scan based on
 * (fileSize + first-64KB hash) — cheap enough to run on-device without
 * hashing entire large video files.
 */
object FileUtils {

    fun humanReadableSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.0f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }

    /** Returns total bytes used by all files in [dir] (non-recursive is fine for our flat download folder). */
    fun folderSizeBytes(dir: File): Long =
        dir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

    /** Cheap fingerprint: size + hash of first 64KB. Good enough to flag likely duplicates without full re-read. */
    fun quickFingerprint(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            val read = input.read(buffer)
            if (read > 0) digest.update(buffer, 0, read)
        }
        return "${file.length()}_${digest.digest().joinToString("") { "%02x".format(it) }}"
    }

    fun findDuplicates(files: List<File>): List<List<File>> {
        return files.filter { it.isFile }
            .groupBy { quickFingerprint(it) }
            .values
            .filter { it.size > 1 }
    }

    fun renameFile(file: File, newName: String): Boolean {
        val target = File(file.parentFile, newName)
        return file.renameTo(target)
    }

    fun moveFile(file: File, destinationDir: File): Boolean {
        if (!destinationDir.exists()) destinationDir.mkdirs()
        val target = File(destinationDir, file.name)
        return file.renameTo(target)
    }

    fun deleteFile(file: File): Boolean = file.delete()
}
