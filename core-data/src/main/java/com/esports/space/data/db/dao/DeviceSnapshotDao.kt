package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.DeviceSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSnapshotDao {
    @Insert
    suspend fun insert(snapshot: DeviceSnapshotEntity)

    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DeviceSnapshotEntity>>

    @Query("SELECT * FROM device_snapshots WHERE timestamp >= :sinceMillis ORDER BY timestamp DESC")
    fun getSnapshotsSince(sinceMillis: Long): Flow<List<DeviceSnapshotEntity>>

    @Query("DELETE FROM device_snapshots WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
