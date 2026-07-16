package com.sandipdigital.videodownloaderpro.domain.model

/**
 * Represents every possible lifecycle state of a download task.
 * Stored as a String name in Room for forward-compatible migrations.
 */
enum class DownloadStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaType { VIDEO, AUDIO }

/**
 * Quality ladder shown to the user when multiple renditions are detected.
 * For direct-link sources, only ORIGINAL is generally available; adaptive
 * sources (HLS/DASH manifests) can expose the rest.
 */
enum class VideoQuality(val label: String, val approxHeight: Int) {
    Q_144("144p", 144),
    Q_240("240p", 240),
    Q_360("360p", 360),
    Q_480("480p", 480),
    Q_720("720p (HD)", 720),
    Q_1080("1080p (Full HD)", 1080),
    Q_1440("1440p (2K)", 1440),
    Q_2160("2160p (4K)", 2160),
    ORIGINAL("Original", Int.MAX_VALUE)
}

enum class AudioFormat { MP3, AAC, ORIGINAL }
