package com.esports.space.agent.perception

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads battery state from [BatteryManager] and thermal data directly from
 * sysfs to avoid a cross-feature dependency on feature-performance.
 */
@Singleton
class DeviceStatePerception @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun batteryPercent(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            ?: return -1
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun cpuTemp(): Float? = readThermalZoneTemp("cpu", "soc")

    fun gpuTemp(): Float? = readThermalZoneTemp("gpu")

    private fun readThermalZoneTemp(vararg keywords: String): Float? {
        return try {
            val thermalDir = File("/sys/class/thermal/")
            if (!thermalDir.exists()) return null
            thermalDir.listFiles()
                ?.filter { it.name.startsWith("thermal_zone") }
                ?.forEach { zone ->
                    val type = File(zone, "type").readText().trim()
                    if (keywords.any { type.contains(it, ignoreCase = true) }) {
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
