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
        val AGENT_THINKING_MODE = stringPreferencesKey("agent_thinking_mode")
        val SPRITE_POS_X = intPreferencesKey("sprite_pos_x")
        val SPRITE_POS_Y = intPreferencesKey("sprite_pos_y")
        val PANEL_OFFSET_X = intPreferencesKey("panel_offset_x")
        val PANEL_OFFSET_Y = intPreferencesKey("panel_offset_y")
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

    val spriteAppearance: Flow<String> =
        context.dataStore.data.map { it[SPRITE_APPEARANCE] ?: "default" }

    val agentFrequency: Flow<Int> =
        context.dataStore.data.map { (it[AGENT_FREQUENCY] ?: "30").toIntOrNull() ?: 30 }
    val agentThinkingMode: Flow<String> =
        context.dataStore.data.map { it[AGENT_THINKING_MODE] ?: "hybrid" }
    val spritePositionX: Flow<Int> = context.dataStore.data.map { it[SPRITE_POS_X] ?: 980 }
    val spritePositionY: Flow<Int> = context.dataStore.data.map { it[SPRITE_POS_Y] ?: 520 }
    val panelOffsetX: Flow<Int> = context.dataStore.data.map { it[PANEL_OFFSET_X] ?: 0 }
    val panelOffsetY: Flow<Int> = context.dataStore.data.map { it[PANEL_OFFSET_Y] ?: 0 }

    suspend fun setSpriteAppearance(appearance: String) {
        context.dataStore.edit { it[SPRITE_APPEARANCE] = appearance }
    }

    suspend fun setAgentFrequency(minutes: Int) {
        context.dataStore.edit { it[AGENT_FREQUENCY] = minutes.toString() }
    }

    suspend fun setAgentThinkingMode(mode: String) {
        context.dataStore.edit { it[AGENT_THINKING_MODE] = mode }
    }

    suspend fun setSpritePosition(x: Int, y: Int) {
        context.dataStore.edit {
            it[SPRITE_POS_X] = x
            it[SPRITE_POS_Y] = y
        }
    }

    suspend fun setPanelOffset(x: Int, y: Int) {
        context.dataStore.edit {
            it[PANEL_OFFSET_X] = x
            it[PANEL_OFFSET_Y] = y
        }
    }
}
