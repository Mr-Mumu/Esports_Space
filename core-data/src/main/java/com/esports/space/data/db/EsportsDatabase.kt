package com.esports.space.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.esports.space.data.db.dao.*
import com.esports.space.data.db.entity.*

@Database(
    entities = [
        GameRecordEntity::class,
        PlaySessionEntity::class,
        DeviceSnapshotEntity::class,
        AgentEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EsportsDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun playSessionDao(): PlaySessionDao
    abstract fun deviceSnapshotDao(): DeviceSnapshotDao
    abstract fun agentEventDao(): AgentEventDao
}
