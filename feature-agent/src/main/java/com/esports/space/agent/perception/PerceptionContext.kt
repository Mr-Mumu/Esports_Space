package com.esports.space.agent.perception

import com.esports.space.common.util.TimeSlot

data class PerceptionContext(
    val currentHour: Int,
    val currentDayOfWeek: Int,
    val timeSlot: TimeSlot,
    val continuousNonGameMinutes: Long,
    val recentGameFrequency: Map<String, Int>,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val cpuTemp: Float?,
    val gpuTemp: Float?,
    val networkLatencyMs: Long?,
    val upcomingCalendarEvents: List<String>,
    val foregroundApp: String?
)
