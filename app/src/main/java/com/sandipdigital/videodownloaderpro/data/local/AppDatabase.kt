package com.sandipdigital.videodownloaderpro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandipdigital.videodownloaderpro.data.local.dao.DownloadDao
import com.sandipdigital.videodownloaderpro.data.local.entity.DownloadEntity

@Database(
    entities = [DownloadEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        const val DATABASE_NAME = "downloads.db"
    }
}
