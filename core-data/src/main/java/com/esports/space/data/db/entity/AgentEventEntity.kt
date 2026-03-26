package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgentEventType { RECOMMENDATION, REMINDER, ALERT }
enum class UserAction { ACCEPTED, DISMISSED, IGNORED }

@Entity(tableName = "agent_events")
data class AgentEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: AgentEventType,
    val triggerSource: String,
    val content: String,
    val userAction: UserAction?
)
