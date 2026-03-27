package com.esports.space.agent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esports.space.agent.recommendation.RecommendationCard
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun RecommendationListPanel(
    events: List<AgentEventEntity>,
    onAccept: (AgentEventEntity) -> Unit,
    onDismiss: (AgentEventEntity) -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 5
) {
    val theme = LocalThemeConfig.current
    val displayEvents = events.take(maxItems)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "智能推荐",
                style = MaterialTheme.typography.titleMedium,
                color = theme.textPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "最近 ${displayEvents.size} 条",
                style = MaterialTheme.typography.labelSmall,
                color = theme.textSecondary
            )
        }

        if (displayEvents.isEmpty()) {
            Text(
                text = "暂无推荐",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayEvents, key = { it.id }) { event ->
                    RecommendationCard(
                        action = event.toTriggeredAction(),
                        onAccept = { onAccept(event) },
                        onDismiss = { onDismiss(event) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

private fun AgentEventEntity.toTriggeredAction() = TriggeredAction(
    ruleId = triggerSource,
    priority = 0,
    type = eventType.name,
    message = content,
    gameFilter = null
)
