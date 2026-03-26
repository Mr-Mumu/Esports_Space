package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_sessions",
    foreignKeys = [ForeignKey(
        entity = GameRecordEntity::class,
        parentColumns = ["packageName"],
        childColumns = ["packageName"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["packageName"])]
)
data class PlaySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long
)
