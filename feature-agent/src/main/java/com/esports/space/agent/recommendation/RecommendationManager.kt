package com.esports.space.agent.recommendation

import com.esports.space.agent.perception.PerceptionEngine
import com.esports.space.agent.rules.Rule
import com.esports.space.agent.rules.RuleEngine
import com.esports.space.agent.rules.RuleParser
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.datastore.UserPreferenceStore
import com.esports.space.network.api.AgentApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationManager @Inject constructor(
    private val perceptionEngine: PerceptionEngine,
    private val ruleEngine: RuleEngine,
    private val ruleParser: RuleParser,
    private val agentApi: AgentApi,
    private val agentEventDao: AgentEventDao,
    private val userPreferenceStore: UserPreferenceStore
) {

    private val _recommendations = MutableSharedFlow<TriggeredAction>(extraBufferCapacity = 8)
    val recommendations: SharedFlow<TriggeredAction> = _recommendations.asSharedFlow()

    private var cachedRules: List<Rule> = emptyList()
    private var lastProactiveTimestamp = 0L
    private var evaluationJob: Job? = null

    companion object {
        private const val EVALUATION_INTERVAL_MS = 5 * 60_000L
        private const val MIN_PROACTIVE_INTERVAL_MS = 30 * 60_000L
    }

    fun start(scope: CoroutineScope) {
        evaluationJob?.cancel()
        evaluationJob = scope.launch {
            syncRules()
            userPreferenceStore.agentEnabled.collectLatest { enabled ->
                if (enabled) runEvaluationLoop()
            }
        }
    }

    fun stop() {
        evaluationJob?.cancel()
        evaluationJob = null
    }

    private suspend fun runEvaluationLoop() {
        while (currentCoroutineContext().isActive) {
            runCatching { evaluate() }
            delay(EVALUATION_INTERVAL_MS)
        }
    }

    private suspend fun evaluate() {
        if (cachedRules.isEmpty()) return
        val context = perceptionEngine.perceive()
        val actions = ruleEngine.evaluate(cachedRules, context)
        val now = System.currentTimeMillis()

        val topAction = actions.firstOrNull() ?: return
        if (now - lastProactiveTimestamp < MIN_PROACTIVE_INTERVAL_MS) return

        lastProactiveTimestamp = now
        val entity = AgentEventEntity(
            timestamp = now,
            eventType = AgentEventType.valueOf(topAction.type),
            triggerSource = topAction.ruleId,
            content = topAction.message,
            userAction = null
        )
        agentEventDao.insert(entity)
        _recommendations.emit(topAction)
    }

    suspend fun syncRules() {
        try {
            val response = agentApi.getRules()
            response.data?.let { data ->
                cachedRules = ruleParser.parse(data.rules)
            }
        } catch (_: Exception) {
            // Network failure — keep using cached rules
        }
    }

    fun recentRecommendations(): Flow<List<AgentEventEntity>> =
        agentEventDao.getRecent(20)
}
