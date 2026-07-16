package com.sandipdigital.videodownloaderpro.data.network

import android.webkit.URLUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

sealed class UrlCheckResult {
    data class Valid(
        val url: String,
        val suggestedFileName: String,
        val contentType: String?,
        val contentLengthBytes: Long,
        val supportsRangeRequests: Boolean
    ) : UrlCheckResult()

    data class Invalid(val reason: String) : UrlCheckResult()
}

/**
 * Validates that a pasted string is a well-formed, safe, direct HTTP(S) URL
 * before it's ever handed to the download engine. This intentionally does
 * NOT attempt to scrape or reverse-engineer third-party platform pages —
 * only direct file links (mp4/m4a/mp3/etc, or a server that responds with a
 * proper media Content-Type) are treated as downloadable.
 *
 * Design goals (privacy-first / safe-by-default):
 *  - Only http/https schemes accepted — no file://, content://, javascript: etc.
 *  - No credentials embedded in the URL are permitted.
 *  - A lightweight HEAD request confirms reachability + size before queuing,
 *    so we never start a doomed download or leak the URL to any third party.
 */
class UrlValidator(private val client: OkHttpClient) {

    private val allowedSchemes = setOf("http", "https")
    private val mediaExtensions = setOf(
        "mp4", "mkv", "webm", "mov", "avi", "3gp", "ts", "m3u8", "mpd",
        "mp3", "m4a", "aac", "wav", "flac", "ogg"
    )

    fun isWellFormed(rawUrl: String): Boolean {
        if (!URLUtil.isValidUrl(rawUrl)) return false
        return try {
            val parsed = URL(rawUrl)
            parsed.protocol.lowercase() in allowedSchemes && parsed.userInfo == null
        } catch (e: Exception) {
            false
        }
    }

    /** Performs a HEAD request to confirm the link resolves to real media. */
    suspend fun probe(rawUrl: String): UrlCheckResult {
        if (!isWellFormed(rawUrl)) {
            return UrlCheckResult.Invalid("Only direct http:// or https:// media links are supported.")
        }

        return try {
            val request = Request.Builder().url(rawUrl).head().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return UrlCheckResult.Invalid("Server responded with ${response.code}.")
                }
                val contentType = response.header("Content-Type")
                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
                val acceptRanges = response.header("Accept-Ranges")?.contains("bytes") == true

                val looksLikeMedia = contentType?.let {
                    it.startsWith("video/") || it.startsWith("audio/") || it.contains("mpegurl")
                } ?: false
                val extensionLooksLikeMedia = URL(rawUrl).path.substringAfterLast('.', "")
                    .lowercase() in mediaExtensions

                if (!looksLikeMedia && !extensionLooksLikeMedia) {
                    return UrlCheckResult.Invalid("This link doesn't appear to point to a media file.")
                }

                UrlCheckResult.Valid(
                    url = rawUrl,
                    suggestedFileName = suggestFileName(rawUrl, contentType),
                    contentType = contentType,
                    contentLengthBytes = contentLength,
                    supportsRangeRequests = acceptRanges
                )
            }
        } catch (e: Exception) {
            UrlCheckResult.Invalid("Couldn't reach the URL: ${e.localizedMessage ?: "unknown error"}")
        }
    }

    private fun suggestFileName(rawUrl: String, contentType: String?): String {
        val path = URL(rawUrl).path
        val lastSegment = path.substringAfterLast('/').ifBlank { "download" }
        val hasExtension = lastSegment.contains('.')
        if (hasExtension) return lastSegment

        val ext = when {
            contentType?.startsWith("video/") == true -> contentType.substringAfter('/')
            contentType?.startsWith("audio/") == true -> contentType.substringAfter('/')
            else -> "bin"
        }
        return "$lastSegment.$ext"
    }
}
