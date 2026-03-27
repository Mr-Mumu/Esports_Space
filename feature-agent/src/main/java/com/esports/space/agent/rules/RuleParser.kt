package com.esports.space.agent.rules

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleParser @Inject constructor() {

    fun parse(rawRules: List<Map<String, Any>>): List<Rule> =
        rawRules.mapNotNull { parseRule(it) }

    private fun parseRule(map: Map<String, Any>): Rule? {
        return try {
            val id = map["id"] as? String ?: return null
            val priority = (map["priority"] as? Number)?.toInt() ?: 0
            val conditionsMap = asMap(map["conditions"]) ?: return null
            val conditions = parseConditionGroup(conditionsMap) ?: return null
            val actionMap = asMap(map["action"]) ?: return null
            val action = parseAction(actionMap) ?: return null
            val cooldown = (map["cooldownMinutes"] as? Number)?.toInt() ?: 30
            val maxDismissals = (map["maxDismissalsPerDay"] as? Number)?.toInt() ?: 3
            Rule(id, priority, conditions, action, cooldown, maxDismissals)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseConditionGroup(map: Map<String, Any>): ConditionGroup? {
        val operator = (map["operator"] as? String)?.uppercase() ?: "AND"
        val items = (map["items"] as? List<*>)?.mapNotNull { item ->
            val m = asMap(item) ?: return@mapNotNull null
            parseCondition(m)
        } ?: return null
        if (items.isEmpty()) return null
        return ConditionGroup(operator, items)
    }

    private fun parseCondition(map: Map<String, Any>): Condition? {
        val dimension = map["dimension"] as? String ?: return null
        val field = map["field"] as? String ?: return null
        val op = map["op"] as? String ?: return null
        val value = map["value"] ?: return null
        return Condition(dimension, field, op, value)
    }

    private fun parseAction(map: Map<String, Any>): RuleAction? {
        val type = map["type"] as? String ?: return null
        val template = map["template"] as? String ?: return null
        val filterMap = asMap(map["gameFilter"])
        val gameFilter = filterMap?.let {
            GameFilter(
                maxSessionMinutes = (it["maxSessionMinutes"] as? Number)?.toInt(),
                tags = (it["tags"] as? List<*>)?.filterIsInstance<String>()
            )
        }
        return RuleAction(type, template, gameFilter)
    }

    @Suppress("UNCHECKED_CAST")
    private fun asMap(obj: Any?): Map<String, Any>? = obj as? Map<String, Any>
}
