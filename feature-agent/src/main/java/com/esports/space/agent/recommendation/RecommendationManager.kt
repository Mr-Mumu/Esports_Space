package com.esports.space.agent.recommendation

import com.esports.space.agent.perception.PerceptionEngine
import com.esports.space.agent.perception.PerceptionContext
import com.esports.space.agent.rules.Rule
import com.esports.space.agent.rules.RuleEngine
import com.esports.space.agent.rules.RuleParser
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.datastore.UserPreferenceStore
import com.esports.space.network.api.AgentApi
import com.esports.space.network.model.AgentEnhanceRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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

        val thinkingMode = userPreferenceStore.agentThinkingMode.first()
        val enrichedAction = enrichActionWithThought(topAction, context, thinkingMode)

        lastProactiveTimestamp = now
        val entity = AgentEventEntity(
            timestamp = now,
            eventType = AgentEventType.valueOf(enrichedAction.type),
            triggerSource = enrichedAction.ruleId,
            content = enrichedAction.message,
            userAction = null
        )
        agentEventDao.insert(entity)
        _recommendations.emit(enrichedAction)
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

    private suspend fun enrichActionWithThought(
        action: TriggeredAction,
        context: PerceptionContext,
        thinkingMode: String
    ): TriggeredAction {
        val localThought = composeLocalThought(action.message, context)
        val cloudThought = if (thinkingMode == "cloud" || thinkingMode == "hybrid") {
            requestCloudThought(context)
        } else {
            null
        }

        val finalMessage = when (thinkingMode) {
            "local" -> localThought
            "cloud" -> cloudThought ?: localThought
            else -> if (cloudThought.isNullOrBlank()) localThought else "$localThought\n$cloudThought"
        }

        return action.copy(message = finalMessage)
    }

    private fun composeLocalThought(baseMessage: String, context: PerceptionContext): String {
        val timeHint = when (context.timeSlot.name) {
            "MORNING" -> "晨间状态不错，适合热手。"
            "AFTERNOON" -> "下午段注意节奏，稳住操作。"
            "EVENING" -> "黄金时段已到，可以冲一把。"
            else -> "夜深了，建议轻量放松局。"
        }
        val deviceHint = when {
            context.batteryPercent <= 20 && !context.isCharging -> "电量偏低，先连充更稳。"
            (context.cpuTemp ?: 0f) >= 45f -> "设备温度有点高，建议降低负载。"
            else -> "当前设备状态稳定。"
        }
        return "$baseMessage\n$timeHint $deviceHint"
    }

    private suspend fun requestCloudThought(context: PerceptionContext): String? {
        return runCatching {
            val summary = buildString {
                append("hour=${context.currentHour};")
                append("slot=${context.timeSlot};")
                append("nonGame=${context.continuousNonGameMinutes};")
                append("battery=${context.batteryPercent};")
                append("charging=${context.isCharging};")
                append("calendar=${context.upcomingCalendarEvents.size}")
            }
            val response = agentApi.enhance(
                AgentEnhanceRequest(
                    dimensions = listOf("time", "usage", "device", "calendar"),
                    behaviorSummary = summary
                )
            )
            response.data?.recommendations?.firstOrNull()?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
