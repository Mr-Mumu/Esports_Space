package com.esports.space.agent.sprite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.esports.space.agent.rules.TriggeredAction
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun BubbleDialog(
    visible: Boolean,
    action: TriggeredAction?,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current

    AnimatedVisibility(
        visible = visible && action != null,
        enter = scaleIn(
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(),
        exit = scaleOut(
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeOut(),
        modifier = modifier
    ) {
        action?.let { act ->
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        theme.primaryAccent.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = iconForType(act.type),
                        contentDescription = null,
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = act.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    FilledTonalButton(onClick = onAccept) {
                        Text(actionLabel(act.type))
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(onClick = onDismiss) {
                        Text("稍后再说")
                    }
                }
            }
        }
    }
}

private fun actionLabel(type: String): String = when (type) {
    "RECOMMENDATION" -> "开始游戏"
    "REMINDER" -> "去查看"
    "ALERT" -> "去休息"
    else -> "确定"
}

private fun iconForType(type: String): ImageVector = when (type) {
    "RECOMMENDATION" -> Icons.Default.Gamepad
    "REMINDER" -> Icons.Default.Visibility
    "ALERT" -> Icons.Default.Nightlight
    else -> Icons.Default.Gamepad
}
