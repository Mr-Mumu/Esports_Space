package com.esports.space.games.data

import android.content.Context
import android.content.Intent
import com.esports.space.data.datastore.UserPreferenceStore
import com.esports.space.data.db.dao.GameRecordDao
import com.esports.space.data.db.dao.PlaySessionDao
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import com.esports.space.games.domain.GameClassifier
import com.esports.space.games.domain.model.ClassifiedGame
import com.esports.space.network.api.GamesApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameRecordDao: GameRecordDao,
    private val playSessionDao: PlaySessionDao,
    private val gamesApi: GamesApi,
    private val scanner: GameScanner,
    private val classifier: GameClassifier,
    private val prefStore: UserPreferenceStore
) {

    companion object {
        private const val WHITELIST_TTL = 24L * 60 * 60 * 1000
        private const val NEW_GAMES_TTL = 12L * 60 * 60 * 1000
    }

    fun classifiedGames(): Flow<List<ClassifiedGame>> =
        gameRecordDao.getAll().map { records ->
            val sessionsMap = records.associate { record ->
                record.packageName to playSessionDao.getByPackage(record.packageName).first()
            }
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            classifier.classify(records, sessionsMap, hour)
        }

    suspend fun refreshWhitelist() {
        val lastRefresh = prefStore.whitelistCacheTime.first()
        if (System.currentTimeMillis() - lastRefresh < WHITELIST_TTL) return

        try {
            val response = gamesApi.getWhitelist()
            val whitelist = response.data.orEmpty().toSet()
            prefStore.setWhitelistCache(whitelist)
            syncInstalledGames(whitelist)
        } catch (_: Exception) {
            val cached = prefStore.gameWhitelist.first()
            if (cached.isNotEmpty()) syncInstalledGames(cached)
        }
    }

    suspend fun refreshNewGames() {
        val lastRefresh = prefStore.newGamesCacheTime.first()
        if (System.currentTimeMillis() - lastRefresh < NEW_GAMES_TTL) return

        try {
            val response = gamesApi.getNewGames()
            val newGames = response.data.orEmpty()
            for (game in newGames) {
                val existing = gameRecordDao.getAll().first()
                    .find { it.packageName == game.packageName }
                if (existing == null) {
                    gameRecordDao.upsert(
                        GameRecordEntity(
                            packageName = game.packageName,
                            displayName = game.displayName,
                            iconUri = game.iconUrl,
                            posterUri = game.posterUrl,
                            category = GameCategory.NEW,
                            totalPlayTime = 0,
                            lastPlayedAt = 0,
                            launchCount = 0,
                            pinned = false
                        )
                    )
                }
            }
            prefStore.setNewGamesCacheTime(System.currentTimeMillis())
        } catch (_: Exception) {
            // Use cached data on failure
        }
    }

    suspend fun addGameManually(packageName: String) {
        val pm = context.packageManager
        val appInfo = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (_: Exception) {
            return
        }
        val label = pm.getApplicationLabel(appInfo).toString()
        val iconUri = if (appInfo.icon != 0) {
            "android.resource://$packageName/${appInfo.icon}"
        } else ""

        gameRecordDao.upsert(
            GameRecordEntity(
                packageName = packageName,
                displayName = label,
                iconUri = iconUri,
                posterUri = null,
                category = GameCategory.INFREQUENT,
                totalPlayTime = 0,
                lastPlayedAt = 0,
                launchCount = 0,
                pinned = false
            )
        )
    }

    suspend fun launchGame(packageName: String, context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        val record = gameRecordDao.getAll().first().find { it.packageName == packageName } ?: return
        gameRecordDao.update(
            record.copy(
                lastPlayedAt = System.currentTimeMillis(),
                launchCount = record.launchCount + 1
            )
        )
    }

    suspend fun togglePin(packageName: String) {
        val record = gameRecordDao.getAll().first()
            .find { it.packageName == packageName } ?: return
        gameRecordDao.update(record.copy(pinned = !record.pinned))
    }

    suspend fun removeGame(packageName: String) {
        val record = gameRecordDao.getAll().first()
            .find { it.packageName == packageName } ?: return
        gameRecordDao.delete(record)
    }

    fun getInstalledApps(): List<ScannedGame> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).mapNotNull { ri ->
            val appInfo = ri.activityInfo.applicationInfo ?: return@mapNotNull null
            val iconUri = if (appInfo.icon != 0) {
                "android.resource://${appInfo.packageName}/${appInfo.icon}"
            } else ""
            ScannedGame(
                packageName = appInfo.packageName,
                displayName = ri.loadLabel(pm).toString(),
                iconUri = iconUri
            )
        }
    }

    private suspend fun syncInstalledGames(whitelist: Set<String>) {
        val scanned = scanner.scanInstalledGames(whitelist)
        val existing = gameRecordDao.getAll().first().map { it.packageName }.toSet()

        for (game in scanned) {
            if (game.packageName !in existing) {
                gameRecordDao.upsert(
                    GameRecordEntity(
                        packageName = game.packageName,
                        displayName = game.displayName,
                        iconUri = game.iconUri,
                        posterUri = null,
                        category = GameCategory.INFREQUENT,
                        totalPlayTime = 0,
                        lastPlayedAt = 0,
                        launchCount = 0,
                        pinned = false
                    )
                )
            }
        }
    }
}
