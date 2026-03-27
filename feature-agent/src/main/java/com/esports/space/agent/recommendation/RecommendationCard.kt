package com.esports.space.agent.recommendation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.ui.component.GlassCard
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun RecommendationCard(
    action: TriggeredAction,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val icon = iconForType(action.type)

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.textPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = onAccept) {
                Text("开始")
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "忽略",
                    tint = theme.textSecondary
                )
            }
        }
    }
}

private fun iconForType(type: String): ImageVector = when (type) {
    "RECOMMENDATION" -> Icons.Default.Gamepad
    "REMINDER" -> Icons.Default.Visibility
    "ALERT" -> Icons.Default.Nightlight
    else -> Icons.Default.Gamepad
}
