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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.space.datacenter.data.HourlyHeatStat
import com.esports.space.ui.theme.LocalThemeConfig

private val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

@Composable
fun HeatMapView(
    data: List<HourlyHeatStat>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalThemeConfig.current
    val bg = theme.background
    val accent = theme.primaryAccent
    val labelColor = theme.textSecondary

    val matrix = remember(data) {
        val m = Array(7) { LongArray(24) }
        for (cell in data) {
            if (cell.dayOfWeek in 0..6 && cell.hour in 0..23) {
                m[cell.dayOfWeek][cell.hour] = cell.totalMs
            }
        }
        m
    }
    val maxMs = remember(matrix) {
        var max = 1L
        for (d in 0 until 7) {
            for (h in 0 until 24) {
                max = maxOf(max, matrix[d][h])
            }
        }
        max
    }

    val leftLabelWidth = 40.dp
    val cellGap = 1.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = leftLabelWidth),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            (0 until 24).forEach { h ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (h % 3 == 0) {
                        Text(
                            "$h",
                            color = labelColor,
                            fontSize = 8.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(leftLabelWidth),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Text(
                            label,
                            color = labelColor,
                            fontSize = 9.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height((18.dp * 7) + (cellGap * 6)),
            ) {
                val cols = 24
                val rows = 7
                val gapPx = cellGap.toPx()
                val cw = (size.width - gapPx * (cols - 1)) / cols
                val ch = (size.height - gapPx * (rows - 1)) / rows
                for (d in 0 until rows) {
                    for (h in 0 until cols) {
                        val v = matrix[d][h]
                        val t = (v / maxMs.toFloat()).coerceIn(0f, 1f)
                        val color = lerp(bg, accent, t)
                        val left = h * (cw + gapPx)
                        val top = d * (ch + gapPx)
                        drawRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(cw, ch),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = leftLabelWidth, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("低", color = labelColor, fontSize = 9.sp)
            Text("高", color = labelColor, fontSize = 9.sp)
        }
    }
}
