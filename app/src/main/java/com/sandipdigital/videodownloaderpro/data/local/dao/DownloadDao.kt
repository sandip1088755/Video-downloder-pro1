package com.sandipdigital.videodownloaderpro.data.local.dao

import androidx.room.*
import com.sandipdigital.videodownloaderpro.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun observeByStatus(statuses: List<String>): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE sourceUrl = :url AND status = 'COMPLETED' LIMIT 1")
    suspend fun findCompletedByUrl(url: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadEntity)

    @Query(
        "UPDATE downloads SET status = :status, progressPercent = :progress, " +
            "bytesDownloaded = :bytes, totalBytes = :total, speedBytesPerSec = :speed, " +
            "etaSeconds = :eta WHERE id = :id"
    )
    suspend fun updateProgress(
        id: String,
        status: String,
        progress: Int,
        bytes: Long,
        total: Long,
        speed: Long,
        eta: Long
    )

    @Query("UPDATE downloads SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, error: String? = null)

    @Query("UPDATE downloads SET status = 'COMPLETED', completedAt = :completedAt, progressPercent = 100 WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long)

    @Query("UPDATE downloads SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Delete
    suspend fun delete(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    // Duplicate detection: same title + total size already downloaded
    @Query(
        "SELECT * FROM downloads WHERE title = :title AND totalBytes = :size " +
            "AND status = 'COMPLETED' LIMIT 1"
    )
    suspend fun findDuplicate(title: String, size: Long): DownloadEntity?
}
