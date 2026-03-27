package com.esports.space.agent.rules

import com.esports.space.agent.perception.PerceptionContext
import com.esports.space.common.util.TimeSlot
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.db.entity.UserAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    private fun fakeDao(
        dismissalCount: Int = 0,
        cooldownCount: Int = 0
    ): AgentEventDao = object : AgentEventDao {
        override suspend fun insert(event: AgentEventEntity) {}
        override fun getRecent(limit: Int): Flow<List<AgentEventEntity>> = flowOf(emptyList())
        override suspend fun updateUserAction(id: Long, action: UserAction) {}
        override suspend fun countByTypeAndAction(
            type: AgentEventType,
            action: UserAction,
            since: Long
        ): Int = when (action) {
            UserAction.DISMISSED -> dismissalCount
            else -> cooldownCount
        }
        override suspend fun deleteOlderThan(before: Long) {}
    }

    private fun context(
        nonGameMinutes: Long = 0,
        battery: Int = 100,
        cpuTemp: Float? = null
    ) = PerceptionContext(
        currentHour = 22,
        currentDayOfWeek = 3,
        timeSlot = TimeSlot.EVENING,
        continuousNonGameMinutes = nonGameMinutes,
        recentGameFrequency = emptyMap(),
        batteryPercent = battery,
        isCharging = false,
        cpuTemp = cpuTemp,
        gpuTemp = null,
        networkLatencyMs = null,
        upcomingCalendarEvents = emptyList(),
        foregroundApp = null
    )

    private val fatigueRule = Rule(
        id = "fatigue",
        priority = 10,
        conditions = ConditionGroup(
            "AND",
            listOf(Condition("usage", "continuousNonGameMinutes", ">=", 120))
        ),
        action = RuleAction("ALERT", "已连续使用{nonGameMinutes}分钟"),
        cooldownMinutes = 60,
        maxDismissalsPerDay = 3
    )

    private val lowBatteryRule = Rule(
        id = "low_battery",
        priority = 8,
        conditions = ConditionGroup(
            "AND",
            listOf(Condition("device", "batteryPercent", "<=", 15))
        ),
        action = RuleAction("REMINDER", "电量{battery}%，请充电"),
        cooldownMinutes = 30,
        maxDismissalsPerDay = 5
    )

    @Test
    fun `fatigue rule triggers after 200 continuous non-game minutes`() = runTest {
        val engine = RuleEngine(fakeDao())
        val actions = engine.evaluate(listOf(fatigueRule), context(nonGameMinutes = 200))
        assertEquals(1, actions.size)
        assertEquals("fatigue", actions[0].ruleId)
        assertTrue(actions[0].message.contains("200"))
    }

    @Test
    fun `fatigue rule does NOT trigger below threshold`() = runTest {
        val engine = RuleEngine(fakeDao())
        val actions = engine.evaluate(listOf(fatigueRule), context(nonGameMinutes = 60))
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `low battery rule triggers at 15 percent`() = runTest {
        val engine = RuleEngine(fakeDao())
        val actions = engine.evaluate(listOf(lowBatteryRule), context(battery = 15))
        assertEquals(1, actions.size)
        assertEquals("low_battery", actions[0].ruleId)
    }

    @Test
    fun `low battery rule does NOT trigger at 50 percent`() = runTest {
        val engine = RuleEngine(fakeDao())
        val actions = engine.evaluate(listOf(lowBatteryRule), context(battery = 50))
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `cooldown prevents re-trigger`() = runTest {
        val engine = RuleEngine(fakeDao(cooldownCount = 1))
        val actions = engine.evaluate(listOf(fatigueRule), context(nonGameMinutes = 200))
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `max dismissals blocks rule`() = runTest {
        val engine = RuleEngine(fakeDao(dismissalCount = 3))
        val actions = engine.evaluate(listOf(fatigueRule), context(nonGameMinutes = 200))
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `rules returned in priority order`() = runTest {
        val engine = RuleEngine(fakeDao())
        val ctx = context(nonGameMinutes = 200, battery = 10)
        val actions = engine.evaluate(listOf(lowBatteryRule, fatigueRule), ctx)
        assertEquals(2, actions.size)
        assertEquals("fatigue", actions[0].ruleId)
        assertEquals("low_battery", actions[1].ruleId)
    }

    @Test
    fun `OR condition group triggers when any condition passes`() = runTest {
        val engine = RuleEngine(fakeDao())
        val orRule = Rule(
            id = "or_test",
            priority = 5,
            conditions = ConditionGroup(
                "OR",
                listOf(
                    Condition("device", "batteryPercent", "<=", 15),
                    Condition("device", "cpuTemp", ">", 80.0)
                )
            ),
            action = RuleAction("REMINDER", "设备状态异常"),
            cooldownMinutes = 10,
            maxDismissalsPerDay = 5
        )
        val actions = engine.evaluate(listOf(orRule), context(battery = 50, cpuTemp = 85f))
        assertEquals(1, actions.size)
    }
}
