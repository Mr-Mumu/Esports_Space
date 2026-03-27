package com.esports.space.agent.perception

import android.content.Context
import com.esports.space.common.util.ForegroundAppTracker
import com.esports.space.data.db.dao.PlaySessionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageHabitPerception @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playSessionDao: PlaySessionDao
) {

    /**
     * Approximate continuous non-game time by probing progressively wider
     * windows until play activity is found.
     */
    suspend fun continuousNonGameMinutes(): Long {
        val now = System.currentTimeMillis()
        val intervals = longArrayOf(10, 30, 60, 120, 240)
        for (minutesAgo in intervals) {
            val since = now - minutesAgo * 60_000L
            val playMs = playSessionDao.getTotalPlayTimeSince(since) ?: 0L
            if (playMs > 60_000L) {
                return (minutesAgo - playMs / 60_000L).coerceAtLeast(0L)
            }
        }
        return 240L
    }

    fun currentForegroundApp(): String? =
        ForegroundAppTracker.getCurrentForegroundPackage(context)

    /**
     * Game frequency by package over the last 7 days.
     * Full implementation requires a DAO query to enumerate distinct packages;
     * returns empty for now — the rule engine treats missing data gracefully.
     */
    suspend fun recentGameFrequency(): Map<String, Int> = emptyMap()
}
