package com.esports.space.performance.data

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.io.File

data class DeviceMetrics(
    val cpuTempCelsius: Float?,
    val gpuTempCelsius: Float?,
    val cpuFreqMhz: Int?,
    val gpuFreqMhz: Int?,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val batteryPercent: Int,
    val batteryTemperature: Float?,
    val networkLatencyMs: Long?,
    val timestamp: Long = System.currentTimeMillis()
)

class DeviceMonitor(
    private val context: Context
) {
    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }

    fun metricsFlow(): Flow<DeviceMetrics> = flow {
        while (currentCoroutineContext().isActive) {
            emit(collectMetrics())
            delay(POLL_INTERVAL_MS)
        }
    }

    internal fun collectMetrics(): DeviceMetrics {
        return DeviceMetrics(
            cpuTempCelsius = readCpuTemp(),
            gpuTempCelsius = readGpuTemp(),
            cpuFreqMhz = readCpuFreq(),
            gpuFreqMhz = readGpuFreq(),
            ramUsedMb = readRamUsed(),
            ramTotalMb = readRamTotal(),
            batteryPercent = readBatteryPercent(),
            batteryTemperature = readBatteryTemperature(),
            networkLatencyMs = null // measured separately via HTTP HEAD
        )
    }

    internal fun readCpuTemp(): Float? {
        return try {
            val thermalDir = File("/sys/class/thermal/")
            if (!thermalDir.exists()) return null
            thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
                val type = File(zone, "type").readText().trim()
                if (type.contains("cpu", ignoreCase = true) || type.contains("soc", ignoreCase = true)) {
                    val temp = File(zone, "temp").readText().trim().toFloatOrNull()
                    if (temp != null) return temp / 1000f
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    internal fun readGpuTemp(): Float? {
        return try {
            val thermalDir = File("/sys/class/thermal/")
            if (!thermalDir.exists()) return null
            thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
                val type = File(zone, "type").readText().trim()
                if (type.contains("gpu", ignoreCase = true)) {
                    val temp = File(zone, "temp").readText().trim().toFloatOrNull()
                    if (temp != null) return temp / 1000f
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    internal fun readCpuFreq(): Int? {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (!freqFile.canRead()) return null
            val khz = freqFile.readText().trim().toLongOrNull() ?: return null
            (khz / 1000).toInt()
        } catch (_: Exception) {
            null
        }
    }

    internal fun readGpuFreq(): Int? {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/kernel/gpu/gpu_clock",
            "/sys/devices/platform/gpusysfs/gpu_clock"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.canRead()) {
                    val hz = file.readText().trim().toLongOrNull() ?: continue
                    return (hz / 1_000_000).toInt()
                }
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun readRamUsed(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return (mi.totalMem - mi.availMem) / (1024 * 1024)
    }

    private fun readRamTotal(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.totalMem / (1024 * 1024)
    }

    private fun readBatteryPercent(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return -1
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun readBatteryTemperature(): Float? {
        return try {
            val thermalDir = File("/sys/class/thermal/")
            if (!thermalDir.exists()) return null
            thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
                val type = File(zone, "type").readText().trim()
                if (type.contains("battery", ignoreCase = true)) {
                    val temp = File(zone, "temp").readText().trim().toFloatOrNull()
                    if (temp != null) return temp / 1000f
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
