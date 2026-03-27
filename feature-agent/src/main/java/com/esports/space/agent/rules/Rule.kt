package com.esports.space.agent.rules

data class Rule(
    val id: String,
    val priority: Int,
    val conditions: ConditionGroup,
    val action: RuleAction,
    val cooldownMinutes: Int,
    val maxDismissalsPerDay: Int
)

data class ConditionGroup(
    val operator: String, // "AND" | "OR"
    val items: List<Condition>
)

data class Condition(
    val dimension: String,
    val field: String,
    val op: String, // ==, !=, >, >=, <, <=, between, in
    val value: Any
)

data class RuleAction(
    val type: String, // RECOMMENDATION, REMINDER, ALERT
    val template: String,
    val gameFilter: GameFilter? = null
)

data class GameFilter(
    val maxSessionMinutes: Int? = null,
    val tags: List<String>? = null
)

data class TriggeredAction(
    val ruleId: String,
    val priority: Int,
    val type: String,
    val message: String,
    val gameFilter: GameFilter? = null
)
