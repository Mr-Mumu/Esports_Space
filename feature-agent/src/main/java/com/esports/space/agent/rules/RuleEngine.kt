package com.esports.space.agent.rules

import com.esports.space.agent.perception.PerceptionContext
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.db.entity.UserAction
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor(
    private val agentEventDao: AgentEventDao
) {

    suspend fun evaluate(
        rules: List<Rule>,
        context: PerceptionContext
    ): List<TriggeredAction> {
        val sorted = rules.sortedByDescending { it.priority }
        val result = mutableListOf<TriggeredAction>()
        for (rule in sorted) {
            if (!evaluateConditionGroup(rule.conditions, context)) continue
            if (isInCooldown(rule)) continue
            if (exceededDismissals(rule)) continue

            val message = renderTemplate(rule.action.template, context)
            result.add(
                TriggeredAction(
                    ruleId = rule.id,
                    priority = rule.priority,
                    type = rule.action.type,
                    message = message,
                    gameFilter = rule.action.gameFilter
                )
            )
        }
        return result
    }

    internal fun evaluateConditionGroup(
        group: ConditionGroup,
        context: PerceptionContext
    ): Boolean = when (group.operator) {
        "OR" -> group.items.any { evaluateCondition(it, context) }
        else -> group.items.all { evaluateCondition(it, context) }
    }

    internal fun evaluateCondition(condition: Condition, context: PerceptionContext): Boolean {
        val actual = extractValue(condition.dimension, condition.field, context) ?: return false
        return compareValues(actual, condition.op, condition.value)
    }

    private fun extractValue(dimension: String, field: String, ctx: PerceptionContext): Any? =
        when (dimension) {
            "time" -> when (field) {
                "currentHour" -> ctx.currentHour
                "currentDayOfWeek" -> ctx.currentDayOfWeek
                "timeSlot" -> ctx.timeSlot.name
                else -> null
            }
            "usage" -> when (field) {
                "continuousNonGameMinutes" -> ctx.continuousNonGameMinutes
                "foregroundApp" -> ctx.foregroundApp
                else -> null
            }
            "device" -> when (field) {
                "batteryPercent" -> ctx.batteryPercent
                "isCharging" -> ctx.isCharging
                "cpuTemp" -> ctx.cpuTemp
                "gpuTemp" -> ctx.gpuTemp
                "networkLatencyMs" -> ctx.networkLatencyMs
                else -> null
            }
            "calendar" -> when (field) {
                "upcomingEventCount" -> ctx.upcomingCalendarEvents.size
                else -> null
            }
            else -> null
        }

    @Suppress("UNCHECKED_CAST")
    private fun compareValues(actual: Any, op: String, expected: Any): Boolean {
        return try {
            when (op) {
                "==" -> toComparable(actual) == toComparable(expected)
                "!=" -> toComparable(actual) != toComparable(expected)
                ">" -> toDouble(actual) > toDouble(expected)
                ">=" -> toDouble(actual) >= toDouble(expected)
                "<" -> toDouble(actual) < toDouble(expected)
                "<=" -> toDouble(actual) <= toDouble(expected)
                "between" -> {
                    val range = expected as? List<*> ?: return false
                    if (range.size != 2) return false
                    val v = toDouble(actual)
                    v >= toDouble(range[0]!!) && v <= toDouble(range[1]!!)
                }
                "in" -> {
                    val list = expected as? List<*> ?: return false
                    val strActual = actual.toString()
                    list.any { it.toString() == strActual }
                }
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun toComparable(value: Any): Any = when (value) {
        is Number -> value.toDouble()
        is Boolean -> if (value) 1.0 else 0.0
        else -> value.toString()
    }

    private fun toDouble(value: Any): Double = when (value) {
        is Number -> value.toDouble()
        is Boolean -> if (value) 1.0 else 0.0
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private suspend fun isInCooldown(rule: Rule): Boolean {
        val since = System.currentTimeMillis() - rule.cooldownMinutes * 60_000L
        val eventType = runCatching { AgentEventType.valueOf(rule.action.type) }.getOrNull()
            ?: return false
        val accepted = agentEventDao.countByTypeAndAction(eventType, UserAction.ACCEPTED, since)
        val ignored = agentEventDao.countByTypeAndAction(eventType, UserAction.IGNORED, since)
        return (accepted + ignored) > 0
    }

    private suspend fun exceededDismissals(rule: Rule): Boolean {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eventType = runCatching { AgentEventType.valueOf(rule.action.type) }.getOrNull()
            ?: return false
        val dismissals = agentEventDao.countByTypeAndAction(
            eventType, UserAction.DISMISSED, cal.timeInMillis
        )
        return dismissals >= rule.maxDismissalsPerDay
    }

    private fun renderTemplate(template: String, ctx: PerceptionContext): String =
        template
            .replace("{timeSlot}", ctx.timeSlot.name)
            .replace("{battery}", ctx.batteryPercent.toString())
            .replace("{nonGameMinutes}", ctx.continuousNonGameMinutes.toString())
            .replace("{hour}", ctx.currentHour.toString())
}
