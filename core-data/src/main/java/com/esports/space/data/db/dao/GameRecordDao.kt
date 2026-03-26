package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY lastPlayedAt DESC")
    fun getAll(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records WHERE category = :category")
    fun getByCategory(category: GameCategory): Flow<List<GameRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: GameRecordEntity)

    @Update
    suspend fun update(record: GameRecordEntity)

    @Delete
    suspend fun delete(record: GameRecordEntity)
}
