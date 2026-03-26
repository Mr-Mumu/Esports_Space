package com.esports.space.performance.data

import com.esports.space.data.db.dao.DeviceSnapshotDao
import com.esports.space.data.db.entity.DeviceSnapshotEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceRepository @Inject constructor(
    private val deviceMonitor: DeviceMonitor,
    private val snapshotDao: DeviceSnapshotDao
) {
    companion object {
        const val ECG_BUFFER_SIZE = 120
    }

    fun realtimeMetrics(): Flow<DeviceMetrics> = deviceMonitor.metricsFlow()

    suspend fun saveSnapshot(metrics: DeviceMetrics) {
        val ramPct = if (metrics.ramTotalMb > 0) {
            (metrics.ramUsedMb.toFloat() / metrics.ramTotalMb.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
        val latencyMs = when (val v = metrics.networkLatencyMs) {
            null -> -1
            else -> v.coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        }
        snapshotDao.insert(
            DeviceSnapshotEntity(
                timestamp = metrics.timestamp,
                cpuTemp = metrics.cpuTempCelsius,
                gpuTemp = metrics.gpuTempCelsius,
                cpuFreqMhz = metrics.cpuFreqMhz,
                gpuFreqMhz = metrics.gpuFreqMhz,
                ramUsagePercent = ramPct,
                networkLatencyMs = latencyMs,
                batteryPercent = metrics.batteryPercent
            )
        )
    }

    fun recentSnapshots(hours: Int = 1): Flow<List<DeviceSnapshotEntity>> =
        snapshotDao.getSnapshotsSince(System.currentTimeMillis() - hours * 3_600_000L)
}
