package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.db.entity.UserAction
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentEventDao {
    @Insert
    suspend fun insert(event: AgentEventEntity)

    @Query("SELECT * FROM agent_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<AgentEventEntity>>

    @Query("UPDATE agent_events SET userAction = :action WHERE id = :id")
    suspend fun updateUserAction(id: Long, action: UserAction)

    @Query("SELECT COUNT(*) FROM agent_events WHERE eventType = :type AND userAction = :action AND timestamp >= :since")
    suspend fun countByTypeAndAction(type: AgentEventType, action: UserAction, since: Long): Int

    @Query("DELETE FROM agent_events WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
