package com.esports.space.agent.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleParserTest {

    private val parser = RuleParser()

    @Test
    fun `parse rule with AND conditions from Map`() {
        val raw = listOf<Map<String, Any>>(
            mapOf(
                "id" to "fatigue_alert",
                "priority" to 10,
                "conditions" to mapOf(
                    "operator" to "AND",
                    "items" to listOf(
                        mapOf(
                            "dimension" to "usage",
                            "field" to "continuousNonGameMinutes",
                            "op" to ">=",
                            "value" to 120
                        ),
                        mapOf(
                            "dimension" to "time",
                            "field" to "timeSlot",
                            "op" to "in",
                            "value" to listOf("EVENING", "LATE_NIGHT")
                        )
                    )
                ),
                "action" to mapOf(
                    "type" to "ALERT",
                    "template" to "你已连续使用{nonGameMinutes}分钟，建议休息一下"
                ),
                "cooldownMinutes" to 60,
                "maxDismissalsPerDay" to 3
            )
        )

        val rules = parser.parse(raw)
        assertEquals(1, rules.size)

        val rule = rules[0]
        assertEquals("fatigue_alert", rule.id)
        assertEquals(10, rule.priority)
        assertEquals("AND", rule.conditions.operator)
        assertEquals(2, rule.conditions.items.size)
        assertEquals("usage", rule.conditions.items[0].dimension)
        assertEquals(">=", rule.conditions.items[0].op)
        assertEquals("ALERT", rule.action.type)
        assertEquals(60, rule.cooldownMinutes)
        assertEquals(3, rule.maxDismissalsPerDay)
    }

    @Test
    fun `parse rule with OR conditions`() {
        val raw = listOf<Map<String, Any>>(
            mapOf(
                "id" to "low_power",
                "priority" to 8,
                "conditions" to mapOf(
                    "operator" to "OR",
                    "items" to listOf(
                        mapOf(
                            "dimension" to "device",
                            "field" to "batteryPercent",
                            "op" to "<=",
                            "value" to 15
                        ),
                        mapOf(
                            "dimension" to "device",
                            "field" to "cpuTemp",
                            "op" to ">",
                            "value" to 80
                        )
                    )
                ),
                "action" to mapOf(
                    "type" to "REMINDER",
                    "template" to "电量低于{battery}%，建议插上电源"
                ),
                "cooldownMinutes" to 30,
                "maxDismissalsPerDay" to 5
            )
        )

        val rules = parser.parse(raw)
        assertEquals(1, rules.size)
        assertEquals("OR", rules[0].conditions.operator)
        assertEquals(2, rules[0].conditions.items.size)
        assertEquals("REMINDER", rules[0].action.type)
    }

    @Test
    fun `parse empty or malformed input returns empty`() {
        assertTrue(parser.parse(emptyList()).isEmpty())
        assertTrue(parser.parse(listOf(mapOf("bad" to "data"))).isEmpty())
        assertTrue(
            parser.parse(
                listOf(
                    mapOf(
                        "id" to "x",
                        "conditions" to "not_a_map",
                        "action" to mapOf("type" to "ALERT")
                    )
                )
            ).isEmpty()
        )
    }

    @Test
    fun `parse rule with gameFilter`() {
        val raw = listOf<Map<String, Any>>(
            mapOf(
                "id" to "game_rec",
                "priority" to 5,
                "conditions" to mapOf(
                    "operator" to "AND",
                    "items" to listOf(
                        mapOf(
                            "dimension" to "time",
                            "field" to "timeSlot",
                            "op" to "==",
                            "value" to "EVENING"
                        )
                    )
                ),
                "action" to mapOf(
                    "type" to "RECOMMENDATION",
                    "template" to "来一局游戏吧",
                    "gameFilter" to mapOf(
                        "maxSessionMinutes" to 30,
                        "tags" to listOf("casual", "puzzle")
                    )
                ),
                "cooldownMinutes" to 15,
                "maxDismissalsPerDay" to 2
            )
        )

        val rules = parser.parse(raw)
        assertEquals(1, rules.size)

        val filter = rules[0].action.gameFilter
        assertEquals(30, filter?.maxSessionMinutes)
        assertEquals(listOf("casual", "puzzle"), filter?.tags)
    }
}
