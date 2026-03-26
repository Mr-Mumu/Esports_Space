package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameCategory { PREDICTED, FREQUENT, INFREQUENT, NEW }

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val iconUri: String,
    val posterUri: String?,
    val category: GameCategory,
    val totalPlayTime: Long,
    val lastPlayedAt: Long,
    val launchCount: Int,
    val pinned: Boolean
)
