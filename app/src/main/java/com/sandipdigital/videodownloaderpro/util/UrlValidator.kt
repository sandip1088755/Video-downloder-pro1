package com.sandipdigital.videodownloaderpro.util

import android.util.Patterns
import java.net.URI

/**
 * Validates that a pasted link is a well-formed, secure (HTTPS) direct link.
 * This app only supports direct file links the user has the rights to download —
 * it does not attempt to scrape or bypass protections on third-party platforms.
 */
object UrlValidator {

    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "mov", "webm", "avi", "m4v", "3gp", "ts")
    private val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "aac", "wav", "ogg", "flac", "wma")
    private val STREAM_EXTENSIONS = setOf("m3u8")

    fun isValidHttpsUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (!Patterns.WEB_URL.matcher(trimmed).matches()) return false
        return try {
            val uri = URI(trimmed)
            uri.scheme?.equals("https", ignoreCase = true) == true
        } catch (e: Exception) {
            false
        }
    }

    fun guessFileName(url: String): String {
        return try {
            val uri = URI(url.trim())
            val last = uri.path?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            last ?: "download_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }

    fun extensionOf(fileName: String): String =
        fileName.substringAfterLast('.', "").lowercase()

    fun isVideoExtension(ext: String) = ext in VIDEO_EXTENSIONS || ext in STREAM_EXTENSIONS
    fun isAudioExtension(ext: String) = ext in AUDIO_EXTENSIONS
}
