package com.sandipdigital.videodownloaderpro.data.model

data class RemoteFileInfo(
    val url: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val supportsResume: Boolean,
    val kind: MediaKind
)
