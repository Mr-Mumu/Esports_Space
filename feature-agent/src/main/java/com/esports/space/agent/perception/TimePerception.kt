package com.esports.space.agent.perception

import com.esports.space.common.util.TimeSlot
import com.esports.space.common.util.TimeUtils
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimePerception @Inject constructor() {

    fun currentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    fun currentDayOfWeek(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun currentTimeSlot(): TimeSlot = TimeUtils.getTimeSlot(currentHour())

    fun isWeekend(): Boolean {
        val day = currentDayOfWeek()
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY
    }
}
