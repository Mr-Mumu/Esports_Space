package com.esports.space.common.util

enum class TimeSlot { MORNING, AFTERNOON, EVENING, LATE_NIGHT }

object TimeUtils {
    fun getTimeSlot(hour: Int): TimeSlot = when (hour) {
        in 6..11 -> TimeSlot.MORNING
        in 12..17 -> TimeSlot.AFTERNOON
        in 18..23 -> TimeSlot.EVENING
        else -> TimeSlot.LATE_NIGHT
    }
}
