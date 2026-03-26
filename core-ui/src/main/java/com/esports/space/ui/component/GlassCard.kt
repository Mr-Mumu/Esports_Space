package com.esports.space.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalThemeConfig.current
    val color: Color = theme.surface
    val borderColor: Color = theme.primaryAccent
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, borderColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}
