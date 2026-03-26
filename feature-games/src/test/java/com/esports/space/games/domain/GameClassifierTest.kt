package com.esports.space.games.domain

import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import com.esports.space.data.db.entity.PlaySessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameClassifierTest {

    private lateinit var classifier: GameClassifier
    private val now = System.currentTimeMillis()
    private val oneDay = 24L * 60 * 60 * 1000

    @Before
    fun setup() {
        classifier = GameClassifier()
    }

    @Test
    fun `empty input returns empty output`() {
        val result = classifier.classify(emptyList(), emptyMap(), 14)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `high frequency recent games are classified as PREDICTED`() {
        val records = listOf(
            fakeRecord("game.hot1", launchCount = 50, lastPlayed = now - 1000, totalPlayTime = 5_000_000),
            fakeRecord("game.hot2", launchCount = 40, lastPlayed = now - 2000, totalPlayTime = 4_000_000),
            fakeRecord("game.hot3", launchCount = 30, lastPlayed = now - 3000, totalPlayTime = 3_000_000),
            fakeRecord("game.cold1", launchCount = 1, lastPlayed = now - 30 * oneDay, totalPlayTime = 10_000),
            fakeRecord("game.cold2", launchCount = 2, lastPlayed = now - 60 * oneDay, totalPlayTime = 20_000)
        )

        val sessions = mapOf(
            "game.hot1" to fakeSessions("game.hot1", count = 50, recentCount = 20, currentHour = 14),
            "game.hot2" to fakeSessions("game.hot2", count = 40, recentCount = 15, currentHour = 14),
            "game.hot3" to fakeSessions("game.hot3", count = 30, recentCount = 10, currentHour = 14),
            "game.cold1" to fakeSessions("game.cold1", count = 1, recentCount = 0, currentHour = 3),
            "game.cold2" to fakeSessions("game.cold2", count = 2, recentCount = 0, currentHour = 3)
        )

        val result = classifier.classify(records, sessions, 14)

        val predicted = result.filter { it.category == GameCategory.PREDICTED }
        assertTrue(predicted.size == 3)
        assertTrue(predicted.any { it.packageName == "game.hot1" })
        assertTrue(predicted.any { it.packageName == "game.hot2" })
        assertTrue(predicted.any { it.packageName == "game.hot3" })
    }

    @Test
    fun `old rarely played games are classified as INFREQUENT`() {
        val records = listOf(
            fakeRecord("game.active", launchCount = 100, lastPlayed = now - 1000, totalPlayTime = 10_000_000),
            fakeRecord("game.active2", launchCount = 80, lastPlayed = now - 2000, totalPlayTime = 8_000_000),
            fakeRecord("game.active3", launchCount = 60, lastPlayed = now - 3000, totalPlayTime = 6_000_000),
            fakeRecord("game.active4", launchCount = 40, lastPlayed = now - 4000, totalPlayTime = 4_000_000),
            fakeRecord("game.old", launchCount = 1, lastPlayed = now - 90 * oneDay, totalPlayTime = 5_000)
        )

        val sessions = mapOf(
            "game.active" to fakeSessions("game.active", count = 100, recentCount = 30, currentHour = 14),
            "game.active2" to fakeSessions("game.active2", count = 80, recentCount = 25, currentHour = 14),
            "game.active3" to fakeSessions("game.active3", count = 60, recentCount = 20, currentHour = 14),
            "game.active4" to fakeSessions("game.active4", count = 40, recentCount = 15, currentHour = 14),
            "game.old" to fakeSessions("game.old", count = 1, recentCount = 0, currentHour = 3)
        )

        val result = classifier.classify(records, sessions, 14)
        val oldGame = result.find { it.packageName == "game.old" }!!
        assertEquals(GameCategory.INFREQUENT, oldGame.category)
    }

    @Test
    fun `time slot preference boosts score for matching sessions`() {
        val recordA = fakeRecord("game.evening", launchCount = 10, lastPlayed = now - oneDay, totalPlayTime = 100_000)
        val recordB = fakeRecord("game.morning", launchCount = 10, lastPlayed = now - oneDay, totalPlayTime = 100_000)

        val sessionsA = fakeSessions("game.evening", count = 10, recentCount = 5, currentHour = 20)
        val sessionsB = fakeSessions("game.morning", count = 10, recentCount = 5, currentHour = 8)

        val result = classifier.classify(
            listOf(recordA, recordB),
            mapOf("game.evening" to sessionsA, "game.morning" to sessionsB),
            currentHour = 20
        )

        val eveningScore = result.find { it.packageName == "game.evening" }!!.score
        val morningScore = result.find { it.packageName == "game.morning" }!!.score
        assertTrue("Evening game should score higher at hour 20", eveningScore > morningScore)
    }

    @Test
    fun `NEW category games are preserved`() {
        val records = listOf(
            fakeRecord("game.new", category = GameCategory.NEW),
            fakeRecord("game.regular", launchCount = 5, lastPlayed = now - oneDay, totalPlayTime = 50_000)
        )

        val result = classifier.classify(records, emptyMap(), 14)
        val newGame = result.find { it.packageName == "game.new" }!!
        assertEquals(GameCategory.NEW, newGame.category)
        assertTrue(newGame.isNewRelease)
    }

    private fun fakeRecord(
        pkg: String,
        launchCount: Int = 0,
        lastPlayed: Long = 0L,
        totalPlayTime: Long = 0L,
        category: GameCategory = GameCategory.INFREQUENT
    ) = GameRecordEntity(
        packageName = pkg,
        displayName = pkg.substringAfterLast('.'),
        iconUri = "icon://$pkg",
        posterUri = null,
        category = category,
        totalPlayTime = totalPlayTime,
        lastPlayedAt = lastPlayed,
        launchCount = launchCount,
        pinned = false
    )

    private fun fakeSessions(
        pkg: String,
        count: Int,
        recentCount: Int,
        currentHour: Int
    ): List<PlaySessionEntity> {
        val sessions = mutableListOf<PlaySessionEntity>()
        val hourMs = 60L * 60 * 1000

        for (i in 0 until recentCount) {
            val start = now - i * hourMs
            sessions.add(PlaySessionEntity(
                id = 0,
                packageName = pkg,
                startTime = adjustToHour(start, currentHour),
                endTime = adjustToHour(start, currentHour) + 30 * 60 * 1000,
                durationMs = 30 * 60 * 1000
            ))
        }

        for (i in recentCount until count) {
            val start = now - (10L + i) * oneDay
            sessions.add(PlaySessionEntity(
                id = 0,
                packageName = pkg,
                startTime = start,
                endTime = start + 30 * 60 * 1000,
                durationMs = 30 * 60 * 1000
            ))
        }

        return sessions
    }

    private fun adjustToHour(timestamp: Long, hour: Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, 0)
        }
        return cal.timeInMillis
    }
}
