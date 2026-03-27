package com.esports.space.agent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.agent.recommendation.RecommendationManager
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.UserAction
import com.esports.space.data.datastore.UserPreferenceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentUiState(
    val currentRecommendation: TriggeredAction? = null,
    val recentList: List<AgentEventEntity> = emptyList(),
    val isEnabled: Boolean = true,
    val spriteAppearance: String = "default",
    val frequencyMinutes: Int = 30,
    val showBubble: Boolean = false
)

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val recommendationManager: RecommendationManager,
    private val agentEventDao: AgentEventDao,
    private val userPreferenceStore: UserPreferenceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        observeRecommendations()
        observeRecentEvents()
        recommendationManager.start(viewModelScope)
    }

    private fun observePreferences() {
        userPreferenceStore.agentEnabled
            .onEach { enabled -> _uiState.update { it.copy(isEnabled = enabled) } }
            .launchIn(viewModelScope)

        userPreferenceStore.spriteAppearance
            .onEach { appearance -> _uiState.update { it.copy(spriteAppearance = appearance) } }
            .launchIn(viewModelScope)

        userPreferenceStore.agentFrequency
            .onEach { freq -> _uiState.update { it.copy(frequencyMinutes = freq) } }
            .launchIn(viewModelScope)
    }

    private fun observeRecommendations() {
        recommendationManager.recommendations
            .onEach { action ->
                _uiState.update {
                    it.copy(currentRecommendation = action, showBubble = true)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeRecentEvents() {
        recommendationManager.recentRecommendations()
            .onEach { events -> _uiState.update { it.copy(recentList = events) } }
            .launchIn(viewModelScope)
    }

    fun acceptRecommendation(action: TriggeredAction) {
        viewModelScope.launch {
            updateEventAction(action, UserAction.ACCEPTED)
            _uiState.update { it.copy(showBubble = false, currentRecommendation = null) }
        }
    }

    fun dismissRecommendation(action: TriggeredAction) {
        viewModelScope.launch {
            updateEventAction(action, UserAction.DISMISSED)
            _uiState.update { it.copy(showBubble = false, currentRecommendation = null) }
        }
    }

    fun closeBubble() {
        _uiState.update { it.copy(showBubble = false) }
    }

    fun setAgentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceStore.setAgentEnabled(enabled)
            if (!enabled) {
                recommendationManager.stop()
                _uiState.update { it.copy(showBubble = false, currentRecommendation = null) }
            } else {
                recommendationManager.start(viewModelScope)
            }
        }
    }

    fun setSpriteAppearance(appearance: String) {
        viewModelScope.launch { userPreferenceStore.setSpriteAppearance(appearance) }
    }

    fun setFrequency(minutes: Int) {
        viewModelScope.launch { userPreferenceStore.setAgentFrequency(minutes) }
    }

    private suspend fun updateEventAction(action: TriggeredAction, userAction: UserAction) {
        val recent = _uiState.value.recentList
        val event = recent.firstOrNull {
            it.triggerSource == action.ruleId && it.userAction == null
        }
        event?.let { agentEventDao.updateUserAction(it.id, userAction) }
    }

    override fun onCleared() {
        super.onCleared()
        recommendationManager.stop()
    }
}
