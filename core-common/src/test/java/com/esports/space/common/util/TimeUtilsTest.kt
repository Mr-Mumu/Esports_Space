package com.esports.space.common.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun `getTimeSlot returns MORNING for hour 8`() {
        assertEquals(TimeSlot.MORNING, TimeUtils.getTimeSlot(8))
    }

    @Test
    fun `getTimeSlot returns AFTERNOON for hour 14`() {
        assertEquals(TimeSlot.AFTERNOON, TimeUtils.getTimeSlot(14))
    }

    @Test
    fun `getTimeSlot returns EVENING for hour 19`() {
        assertEquals(TimeSlot.EVENING, TimeUtils.getTimeSlot(19))
    }

    @Test
    fun `getTimeSlot returns LATE_NIGHT for hour 1`() {
        assertEquals(TimeSlot.LATE_NIGHT, TimeUtils.getTimeSlot(1))
    }

    @Test
    fun `getTimeSlot boundary - hour 6 is MORNING`() {
        assertEquals(TimeSlot.MORNING, TimeUtils.getTimeSlot(6))
    }

    @Test
    fun `getTimeSlot boundary - hour 23 is EVENING`() {
        assertEquals(TimeSlot.EVENING, TimeUtils.getTimeSlot(23))
    }

    @Test
    fun `getTimeSlot boundary - hour 0 is LATE_NIGHT`() {
        assertEquals(TimeSlot.LATE_NIGHT, TimeUtils.getTimeSlot(0))
    }
}
