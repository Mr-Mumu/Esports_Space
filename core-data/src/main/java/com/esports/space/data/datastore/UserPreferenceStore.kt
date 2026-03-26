package com.esports.space.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val AGENT_ENABLED = booleanPreferencesKey("agent_enabled")
        val AGENT_FREQUENCY = stringPreferencesKey("agent_frequency")
        val SPRITE_APPEARANCE = stringPreferencesKey("sprite_appearance")
        val WHITELIST_CACHE_TIME = longPreferencesKey("whitelist_cache_time")
        val NEW_GAMES_CACHE_TIME = longPreferencesKey("new_games_cache_time")
        val GAME_WHITELIST = stringSetPreferencesKey("game_whitelist")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "galaxy" }
    val agentEnabled: Flow<Boolean> = context.dataStore.data.map { it[AGENT_ENABLED] ?: true }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun setAgentEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AGENT_ENABLED] = enabled }
    }

    val whitelistCacheTime: Flow<Long> = context.dataStore.data.map { it[WHITELIST_CACHE_TIME] ?: 0L }
    val newGamesCacheTime: Flow<Long> = context.dataStore.data.map { it[NEW_GAMES_CACHE_TIME] ?: 0L }
    val gameWhitelist: Flow<Set<String>> = context.dataStore.data.map { it[GAME_WHITELIST] ?: emptySet() }

    suspend fun setWhitelistCache(whitelist: Set<String>) {
        context.dataStore.edit {
            it[GAME_WHITELIST] = whitelist
            it[WHITELIST_CACHE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun setNewGamesCacheTime(time: Long) {
        context.dataStore.edit { it[NEW_GAMES_CACHE_TIME] = time }
    }
}
