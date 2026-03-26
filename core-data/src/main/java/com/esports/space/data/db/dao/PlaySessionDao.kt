package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.PlaySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaySessionDao {
    @Insert
    suspend fun insert(session: PlaySessionEntity)

    @Query("SELECT * FROM play_sessions WHERE packageName = :pkg ORDER BY startTime DESC")
    fun getByPackage(pkg: String): Flow<List<PlaySessionEntity>>

    @Query("SELECT SUM(durationMs) FROM play_sessions WHERE startTime >= :since")
    suspend fun getTotalPlayTimeSince(since: Long): Long?

    @Query("DELETE FROM play_sessions WHERE startTime < :before")
    suspend fun deleteOlderThan(before: Long)
}
