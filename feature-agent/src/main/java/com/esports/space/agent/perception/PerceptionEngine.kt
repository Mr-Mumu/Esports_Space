package com.esports.space.agent.perception

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerceptionEngine @Inject constructor(
    private val timePerception: TimePerception,
    private val usageHabitPerception: UsageHabitPerception,
    private val deviceStatePerception: DeviceStatePerception,
    private val calendarPerception: CalendarPerception
) {

    suspend fun perceive(): PerceptionContext = PerceptionContext(
        currentHour = timePerception.currentHour(),
        currentDayOfWeek = timePerception.currentDayOfWeek(),
        timeSlot = timePerception.currentTimeSlot(),
        continuousNonGameMinutes = usageHabitPerception.continuousNonGameMinutes(),
        recentGameFrequency = usageHabitPerception.recentGameFrequency(),
        batteryPercent = deviceStatePerception.batteryPercent(),
        isCharging = deviceStatePerception.isCharging(),
        cpuTemp = deviceStatePerception.cpuTemp(),
        gpuTemp = deviceStatePerception.gpuTemp(),
        networkLatencyMs = null,
        upcomingCalendarEvents = calendarPerception.upcomingEvents(),
        foregroundApp = usageHabitPerception.currentForegroundApp()
    )
}
