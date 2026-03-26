package com.esports.space.datacenter.data

import com.esports.space.data.db.dao.GameRecordDao
import com.esports.space.data.db.dao.PlaySessionDao
import com.esports.space.data.db.entity.PlaySessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

data class GameTimeStat(val packageName: String, val displayName: String, val totalMs: Long)

data class DailyPlayStat(val dateLabel: String, val totalMs: Long)

/** [dayOfWeek] 0 = Monday … 6 = Sunday; [hour] 0–23 */
data class HourlyHeatStat(val dayOfWeek: Int, val hour: Int, val totalMs: Long)

@Singleton
class StatsRepository @Inject constructor(
    private val playSessionDao: PlaySessionDao,
    private val gameRecordDao: GameRecordDao,
) {

    private val zone: ZoneId get() = ZoneId.systemDefault()

    private fun startOfTodayMillis(): Long =
        LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun startOfWeekMillis(): Long {
        val today = LocalDate.now(zone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun startOfMonthMillis(): Long {
        val today = LocalDate.now(zone)
        val first = today.withDayOfMonth(1)
        return first.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    suspend fun getTodayPlayTime(): Long =
        playSessionDao.getTotalPlayTimeSince(startOfTodayMillis()) ?: 0L

    suspend fun getWeekPlayTime(): Long =
        playSessionDao.getTotalPlayTimeSince(startOfWeekMillis()) ?: 0L

    suspend fun getMonthPlayTime(): Long =
        playSessionDao.getTotalPlayTimeSince(startOfMonthMillis()) ?: 0L

    suspend fun getWeeklyGameDays(): Int {
        val weekStart = startOfWeekMillis()
        val records = gameRecordDao.getAll().first()
        val distinctDays = mutableSetOf<Long>()
        for (r in records) {
            val sessions = playSessionDao.getByPackage(r.packageName).first()
            for (s in sessions) {
                if (s.startTime >= weekStart) {
                    val epochDay =
                        Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate().toEpochDay()
                    distinctDays.add(epochDay)
                }
            }
        }
        return distinctDays.size
    }

    fun gameTimeDistribution(): Flow<List<GameTimeStat>> =
        gameRecordDao.getAll().map { records ->
            records
                .filter { it.totalPlayTime > 0 }
                .map { GameTimeStat(it.packageName, it.displayName, it.totalPlayTime) }
                .sortedByDescending { it.totalMs }
        }

    private fun allSessionsFlow(): Flow<List<PlaySessionEntity>> =
        gameRecordDao.getAll().flatMapLatest { records ->
            flow {
                emit(records.flatMap { playSessionDao.getByPackage(it.packageName).first() })
            }
        }

    fun dailyPlayHistory(days: Int = 7): Flow<List<DailyPlayStat>> =
        allSessionsFlow().map { sessions ->
            buildDailyPlayHistory(sessions, days, zone)
        }

    fun hourlyHeatmap(): Flow<List<HourlyHeatStat>> =
        allSessionsFlow().map { sessions ->
            buildHourlyHeatmap(sessions, zone)
        }
}

internal fun buildDailyPlayHistory(
    sessions: List<PlaySessionEntity>,
    days: Int,
    zone: ZoneId,
): List<DailyPlayStat> {
    val today = LocalDate.now(zone)
    val startDay = today.minusDays((days - 1).toLong())
    val dayToMs = LinkedHashMap<LocalDate, Long>()
    for (i in 0 until days) {
        dayToMs[startDay.plusDays(i.toLong())] = 0L
    }
    for (s in sessions) {
        val d = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
        if (dayToMs.containsKey(d)) {
            dayToMs[d] = (dayToMs[d] ?: 0L) + s.durationMs
        }
    }
    val formatter = DateTimeFormatter.ofPattern("MM-dd")
    return dayToMs.keys.map { date ->
        DailyPlayStat(date.format(formatter), dayToMs[date] ?: 0L)
    }
}

internal fun buildHourlyHeatmap(
    sessions: List<PlaySessionEntity>,
    zone: ZoneId,
): List<HourlyHeatStat> {
    val matrix = Array(7) { LongArray(24) }
    for (s in sessions) {
        val zdt = Instant.ofEpochMilli(s.startTime).atZone(zone)
        val dow = zdt.dayOfWeek.value - 1
        val hour = zdt.hour
        matrix[dow][hour] += s.durationMs
    }
    return (0 until 7).flatMap { d ->
        (0 until 24).map { h -> HourlyHeatStat(d, h, matrix[d][h]) }
    }
}

internal fun distinctDaysWithSessionsSince(
    sessions: Iterable<PlaySessionEntity>,
    sinceMillis: Long,
    zone: ZoneId,
): Int {
    val distinctDays = mutableSetOf<Long>()
    for (s in sessions) {
        if (s.startTime >= sinceMillis) {
            val epochDay = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate().toEpochDay()
            distinctDays.add(epochDay)
        }
    }
    return distinctDays.size
}
