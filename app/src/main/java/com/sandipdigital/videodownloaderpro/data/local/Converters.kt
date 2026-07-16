package com.sandipdigital.videodownloaderpro.data.local

import androidx.room.TypeConverter
import com.sandipdigital.videodownloaderpro.data.model.DownloadStatus
import com.sandipdigital.videodownloaderpro.data.model.MediaKind

class Converters {
    @TypeConverter
    fun fromStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)

    @TypeConverter
    fun fromKind(value: MediaKind): String = value.name

    @TypeConverter
    fun toKind(value: String): MediaKind = MediaKind.valueOf(value)
}
