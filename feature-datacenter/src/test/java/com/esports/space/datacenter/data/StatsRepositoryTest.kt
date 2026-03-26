package com.esports.space.datacenter.data

import com.esports.space.data.db.dao.GameRecordDao
import com.esports.space.data.db.dao.PlaySessionDao
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import com.esports.space.data.db.entity.PlaySessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class StatsRepositoryTest {

    @Test
    fun `getTodayPlayTime returns sum from dao`() = runTest {
        val zone = ZoneId.systemDefault()
        val startOfToday = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val dao = FakePlaySessionDao(
            sessions = listOf(
                PlaySessionEntity(
                    packageName = "a",
                    startTime = startOfToday + 3_600_000L,
                    endTime = startOfToday + 7_200_000L,
                    durationMs = 3_600_000L,
                ),
            ),
        )
        val repo = StatsRepository(dao, FakeGameRecordDao(emptyList()))
        assertEquals(3_600_000L, repo.getTodayPlayTime())
    }

    @Test
    fun `getTodayPlayTime returns zero when no sessions`() = runTest {
        val dao = FakePlaySessionDao(sessions = emptyList())
        val repo = StatsRepository(dao, FakeGameRecordDao(emptyList()))
        assertEquals(0L, repo.getTodayPlayTime())
    }

    @Test
    fun `getWeeklyGameDays counts distinct days in week`() = runTest {
        val zone = ZoneId.systemDefault()
        val monday = java.time.LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val tuesday = monday.plusDays(1)
        val mondayStart = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val tuesdayStart = tuesday.atStartOfDay(zone).toInstant().toEpochMilli()

        val pkg = "com.game"
        val record = GameRecordEntity(
            packageName = pkg,
            displayName = "G",
            iconUri = "",
            posterUri = null,
            category = GameCategory.FREQUENT,
            totalPlayTime = 0,
            lastPlayedAt = 0,
            launchCount = 0,
            pinned = false,
        )
        val sessions = listOf(
            PlaySessionEntity(
                packageName = pkg,
                startTime = mondayStart + 10_000L,
                endTime = mondayStart + 20_000L,
                durationMs = 10_000L,
            ),
            PlaySessionEntity(
                packageName = pkg,
                startTime = tuesdayStart + 10_000L,
                endTime = tuesdayStart + 20_000L,
                durationMs = 10_000L,
            ),
        )
        val dao = FakePlaySessionDao(byPackage = mapOf(pkg to sessions))
        val repo = StatsRepository(dao, FakeGameRecordDao(listOf(record)))
        assertEquals(2, repo.getWeeklyGameDays())
    }

    @Test
    fun `distinctDaysWithSessionsSince counts unique local days`() {
        val zone = ZoneId.systemDefault()
        val day1 = java.time.LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val day2 = day1.plusDays(1)
        val t1 = day1.atStartOfDay(zone).toInstant().toEpochMilli()
        val t2 = day2.atStartOfDay(zone).toInstant().toEpochMilli()
        val since = day1.atStartOfDay(zone).toInstant().toEpochMilli()
        val sessions = listOf(
            PlaySessionEntity(
                packageName = "a",
                startTime = t1,
                endTime = t1 + 1,
                durationMs = 1000L,
            ),
            PlaySessionEntity(
                packageName = "a",
                startTime = t1 + 5000L,
                endTime = t1 + 6000L,
                durationMs = 1000L,
            ),
            PlaySessionEntity(
                packageName = "a",
                startTime = t2,
                endTime = t2 + 1,
                durationMs = 1000L,
            ),
        )
        assertEquals(2, distinctDaysWithSessionsSince(sessions, since, zone))
    }

    @Test
    fun `distinctDaysWithSessionsSince returns zero when empty`() {
        val zone = ZoneId.systemDefault()
        assertEquals(0, distinctDaysWithSessionsSince(emptyList(), 0L, zone))
    }

    @Test
    fun `buildDailyPlayHistory aggregates by day`() {
        val zone = ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val tYesterday = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
        val tToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val sessions = listOf(
            PlaySessionEntity("a", tYesterday, tYesterday + 1, 3_600_000L),
            PlaySessionEntity("a", tToday, tToday + 1, 1_800_000L),
        )
        val stats = buildDailyPlayHistory(sessions, days = 2, zone = zone)
        assertEquals(2, stats.size)
        val sum = stats.sumOf { it.totalMs }
        assertEquals(5_400_000L, sum)
    }
}

private class FakePlaySessionDao(
    private val sessions: List<PlaySessionEntity> = emptyList(),
    private val byPackage: Map<String, List<PlaySessionEntity>> = emptyMap(),
) : PlaySessionDao {

    override suspend fun insert(session: PlaySessionEntity) = Unit

    override fun getByPackage(pkg: String): Flow<List<PlaySessionEntity>> {
        val list = byPackage[pkg] ?: sessions.filter { it.packageName == pkg }
        return flowOf(list)
    }

    override suspend fun getTotalPlayTimeSince(since: Long): Long? {
        val all = if (byPackage.isNotEmpty()) byPackage.values.flatten() else sessions
        val sum = all.filter { it.startTime >= since }.sumOf { it.durationMs }
        return if (sum == 0L) null else sum
    }

    override suspend fun deleteOlderThan(before: Long) = Unit
}

private class FakeGameRecordDao(
    private val records: List<GameRecordEntity>,
) : GameRecordDao {

    override fun getAll(): Flow<List<GameRecordEntity>> = flowOf(records)

    override fun getByCategory(category: GameCategory): Flow<List<GameRecordEntity>> =
        flowOf(records.filter { it.category == category })

    override suspend fun upsert(record: GameRecordEntity) = Unit

    override suspend fun update(record: GameRecordEntity) = Unit

    override suspend fun delete(record: GameRecordEntity) = Unit
}
