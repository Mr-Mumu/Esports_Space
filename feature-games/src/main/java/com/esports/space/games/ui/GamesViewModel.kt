package com.esports.space.games.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.games.data.GameRepository
import com.esports.space.games.data.ScannedGame
import com.esports.space.games.domain.model.ClassifiedGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GamesUiState(
    val games: List<ClassifiedGame> = emptyList(),
    val installedApps: List<ScannedGame> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        observeGames()
        refresh()
    }

    private fun observeGames() {
        viewModelScope.launch {
            repository.classifiedGames()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { classified ->
                    _uiState.update {
                        it.copy(games = classified, isLoading = false, error = null)
                    }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                repository.refreshWhitelist()
                repository.refreshNewGames()
            } catch (_: Exception) {
                // Failures are non-fatal; cached data remains available
            }
        }
    }

    fun launchGame(packageName: String, context: Context) {
        viewModelScope.launch {
            repository.launchGame(packageName, context)
        }
    }

    fun togglePin(packageName: String) {
        viewModelScope.launch { repository.togglePin(packageName) }
    }

    fun removeGame(packageName: String) {
        viewModelScope.launch { repository.removeGame(packageName) }
    }

    fun addManually(packageName: String) {
        viewModelScope.launch { repository.addGameManually(packageName) }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            val existing = _uiState.value.games.map { it.packageName }.toSet()
            _uiState.update {
                it.copy(installedApps = apps.filter { a -> a.packageName !in existing })
            }
        }
    }
}
