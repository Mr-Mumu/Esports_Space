package com.esports.space.common.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

object ForegroundAppTracker {
    fun getCurrentForegroundPackage(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - TimeUnit.MINUTES.toMillis(5),
            now
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
