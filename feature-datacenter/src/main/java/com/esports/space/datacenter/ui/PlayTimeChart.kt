package com.esports.space.datacenter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.space.datacenter.data.DailyPlayStat
import com.esports.space.ui.theme.LocalThemeConfig
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun PlayTimeChart(
    data: List<DailyPlayStat>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalThemeConfig.current
    val lineColor = theme.primaryAccent
    val labelColor = theme.textSecondary

    val chartHeight = 200.dp
    val yLabelWidth = 36.dp
    val xLabelHeight = 22.dp
    val padH = 8.dp

    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(chartHeight + xLabelHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text("暂无数据", color = labelColor, fontSize = 13.sp)
        }
        return
    }

    val maxMs = remember(data) {
        val m = data.maxOfOrNull { it.totalMs } ?: 0L
        max(m, 1L)
    }
    val maxHours = remember(maxMs) { ceil(maxMs / 3_600_000.0).toInt().coerceAtLeast(1) }

    val yLabelCount = 5
    val yLabels = remember(maxHours) {
        (0 until yLabelCount).map { idx ->
            val hh = (maxHours * (yLabelCount - 1 - idx) / (yLabelCount - 1f)).toInt()
            "${hh}h"
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + xLabelHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .width(yLabelWidth)
                    .height(chartHeight),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                yLabels.forEach { label ->
                    Text(
                        label,
                        color = labelColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .padding(horizontal = padH),
                ) {
                    val w = size.width
                    val h = size.height
                    val n = max(data.size, 1)
                    val maxH = maxHours * 3_600_000L
                    fun xAt(i: Int): Float = if (n <= 1) w / 2f else w * i / (n - 1).toFloat()
                    fun yAt(ms: Long): Float = h - (ms / maxH.toFloat()) * h

                    val points = data.mapIndexed { i, d ->
                        Offset(xAt(i), yAt(d.totalMs))
                    }

                    if (points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, h)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.35f),
                                    lineColor.copy(alpha = 0.02f),
                                ),
                                startY = 0f,
                                endY = h,
                            ),
                        )

                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx()),
                        )

                        points.forEach { p ->
                            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = p)
                            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 2.dp.toPx(), center = p)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(xLabelHeight)
                        .padding(horizontal = padH),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    data.forEach { d ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                d.dateLabel,
                                color = labelColor,
                                fontSize = 9.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
