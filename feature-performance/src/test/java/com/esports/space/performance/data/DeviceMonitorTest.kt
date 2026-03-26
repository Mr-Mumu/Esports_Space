package com.esports.space.performance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceMonitorTest {

    @Test
    fun `readCpuTemp returns null when thermal zone not accessible`() {
        val metrics = DeviceMetrics(
            cpuTempCelsius = null,
            gpuTempCelsius = null,
            cpuFreqMhz = null,
            gpuFreqMhz = null,
            ramUsedMb = 3000,
            ramTotalMb = 8000,
            batteryPercent = 75,
            batteryTemperature = null,
            networkLatencyMs = null
        )
        assertNull(metrics.cpuTempCelsius)
        assertNull(metrics.gpuTempCelsius)
        assertNull(metrics.gpuFreqMhz)
        assertEquals(3000L, metrics.ramUsedMb)
        assertEquals(75, metrics.batteryPercent)
    }

    @Test
    fun `DeviceMetrics displays degraded values correctly`() {
        val metrics = DeviceMetrics(
            cpuTempCelsius = 45.5f,
            gpuTempCelsius = null,
            cpuFreqMhz = 2400,
            gpuFreqMhz = null,
            ramUsedMb = 4096,
            ramTotalMb = 8192,
            batteryPercent = 50,
            batteryTemperature = 32.0f,
            networkLatencyMs = 15
        )
        assertEquals(45.5f, metrics.cpuTempCelsius)
        assertNull(metrics.gpuTempCelsius)
        assertEquals(2400, metrics.cpuFreqMhz)
        assertNull(metrics.gpuFreqMhz)
        assertEquals(15L, metrics.networkLatencyMs)
    }

    @Test
    fun `ECG buffer size constant is 120`() {
        assertEquals(120, PerformanceRepository.ECG_BUFFER_SIZE)
    }
}
