package com.esports.space.games.domain

import com.esports.space.common.util.TimeUtils
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import com.esports.space.data.db.entity.PlaySessionEntity
import com.esports.space.games.domain.model.ClassifiedGame
import java.util.Calendar
import javax.inject.Inject

class GameClassifier @Inject constructor() {

    fun classify(
        records: List<GameRecordEntity>,
        sessionsByPackage: Map<String, List<PlaySessionEntity>>,
        currentHour: Int
    ): List<ClassifiedGame> {
        if (records.isEmpty()) return emptyList()

        val currentSlot = TimeUtils.getTimeSlot(currentHour)
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000

        val newReleases = records.filter { it.category == GameCategory.NEW }
        val scorable = records.filter { it.category != GameCategory.NEW }

        val rawEntries = scorable.map { record ->
            val sessions = sessionsByPackage[record.packageName].orEmpty()
            val totalSessions = sessions.size

            val slotMatchCount = sessions.count { session ->
                val hour = Calendar.getInstance()
                    .apply { timeInMillis = session.startTime }
                    .get(Calendar.HOUR_OF_DAY)
                TimeUtils.getTimeSlot(hour) == currentSlot
            }
            val timeSlotPref = if (totalSessions > 0) slotMatchCount.toDouble() / totalSessions else 0.0

            val recentCount = sessions.count { it.startTime >= sevenDaysAgo }.toDouble()

            val daysSinceLastPlayed =
                ((now - record.lastPlayedAt).coerceAtLeast(0) / (24.0 * 60 * 60 * 1000))
            val freshness = 1.0 / (daysSinceLastPlayed + 1)

            RawEntry(record, timeSlotPref, recentCount, freshness, record.totalPlayTime.toDouble())
        }

        val maxRecent = rawEntries.maxOfOrNull { it.recentFreq }?.coerceAtLeast(1.0) ?: 1.0
        val maxFresh = rawEntries.maxOfOrNull { it.freshness }?.coerceAtLeast(Double.MIN_VALUE) ?: 1.0
        val maxPlay = rawEntries.maxOfOrNull { it.totalPlay }?.coerceAtLeast(1.0) ?: 1.0

        val scored = rawEntries.map { e ->
            val score = 0.4 * e.timeSlotPref +
                    0.3 * (e.recentFreq / maxRecent) +
                    0.2 * (e.freshness / maxFresh) +
                    0.1 * (e.totalPlay / maxPlay)
            ScoredEntry(e.record, score)
        }.sortedByDescending { it.score }

        val scores = scored.map { it.score }
        val median = if (scores.isNotEmpty()) scores[scores.size / 2] else 0.0

        val classified = scored.mapIndexed { index, entry ->
            val category = when {
                entry.record.pinned -> GameCategory.PREDICTED
                index < 3 -> GameCategory.PREDICTED
                entry.score > median -> GameCategory.FREQUENT
                else -> GameCategory.INFREQUENT
            }
            entry.record.toClassified(category, entry.score)
        }

        val newClassified = newReleases.map { record ->
            record.toClassified(GameCategory.NEW, 0.0, isNewRelease = true)
        }

        return classified + newClassified
    }

    private fun GameRecordEntity.toClassified(
        cat: GameCategory,
        score: Double,
        isNewRelease: Boolean = false
    ) = ClassifiedGame(
        packageName = packageName,
        displayName = displayName,
        iconUri = iconUri,
        posterUri = posterUri,
        category = cat,
        score = score,
        isNewRelease = isNewRelease
    )

    private data class RawEntry(
        val record: GameRecordEntity,
        val timeSlotPref: Double,
        val recentFreq: Double,
        val freshness: Double,
        val totalPlay: Double
    )

    private data class ScoredEntry(val record: GameRecordEntity, val score: Double)
}
