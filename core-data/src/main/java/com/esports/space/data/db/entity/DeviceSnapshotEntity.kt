package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_snapshots")
data class DeviceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val cpuTemp: Float?,
    val gpuTemp: Float?,
    val cpuFreqMhz: Int?,
    val gpuFreqMhz: Int?,
    val ramUsagePercent: Float,
    val networkLatencyMs: Int,
    val batteryPercent: Int
)
