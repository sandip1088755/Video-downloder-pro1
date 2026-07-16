package com.sandipdigital.videodownloaderpro.data.model

enum class DownloadStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaKind {
    VIDEO,
    AUDIO,
    OTHER
}
