package com.esports.space.datacenter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.space.datacenter.data.GameTimeStat
import com.esports.space.ui.theme.LocalThemeConfig
import kotlin.math.min

@Composable
fun GamePieChart(
    data: List<GameTimeStat>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalThemeConfig.current
    val palette = remember(theme) {
        listOf(
            theme.primaryAccent,
            theme.secondaryAccent,
            theme.liveIndicator,
            theme.primaryAccent.copy(alpha = 0.75f),
            theme.secondaryAccent.copy(alpha = 0.75f),
            theme.liveIndicator.copy(alpha = 0.75f),
        )
    }

    val totalMs = data.sumOf { it.totalMs }
    val totalHours = totalMs / 3_600_000f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (data.isEmpty() || totalMs == 0L) {
                Text(
                    "暂无游戏时长数据",
                    color = theme.textSecondary,
                    fontSize = 14.sp,
                )
            } else {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val stroke = 28.dp.toPx()
                    val diameter = min(size.width, size.height)
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)
                    var start = -90f
                    data.forEachIndexed { i, item ->
                        val sweep = 360f * (item.totalMs / totalMs.toFloat())
                        val color = palette[i % palette.size]
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize,
                        )
                        start += sweep
                    }
                    drawCircle(
                        color = theme.background,
                        radius = diameter / 2f - stroke,
                        center = Offset(size.width / 2f, size.height / 2f),
                    )
                }
                Text(
                    String.format("%.1f 小时", totalHours),
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (data.isNotEmpty() && totalMs > 0L) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                data.forEachIndexed { i, item ->
                    val pct = 100f * item.totalMs / totalMs
                    val color = palette[i % palette.size]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = color, radius = size.minDimension / 2f)
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            item.displayName,
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Text(
                            String.format("%.0f%%", pct),
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
