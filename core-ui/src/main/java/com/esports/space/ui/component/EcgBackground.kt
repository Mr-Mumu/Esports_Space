package com.esports.space.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.esports.space.ui.theme.LocalThemeConfig
import kotlin.math.max

@Composable
fun EcgBackground(
    cpuFreqHistory: List<Float>,
    gpuFreqHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val cpuColor = theme.primaryAccent.copy(alpha = 0.3f)
    val gpuColor = theme.secondaryAccent.copy(alpha = 0.3f)
    val gridColor = theme.textSecondary.copy(alpha = 0.08f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 16.dp.toPx()
        val gridStep = 40.dp.toPx()

        var x = 0f
        while (x <= w) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += gridStep
        }
        var y = 0f
        while (y <= h) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += gridStep
        }

        if (cpuFreqHistory.size >= 2) {
            val path = buildSmoothWavePath(cpuFreqHistory, w, h, pad)
            drawPath(
                path = path,
                color = cpuColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        if (gpuFreqHistory.size >= 2) {
            val path = buildSmoothWavePath(gpuFreqHistory, w, h, pad)
            drawPath(
                path = path,
                color = gpuColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

private fun buildSmoothWavePath(
    samples: List<Float>,
    width: Float,
    height: Float,
    pad: Float
): Path {
    val path = Path()
    val minV = samples.minOrNull() ?: 0f
    val maxV = samples.maxOrNull() ?: 1f
    val span = max(maxV - minV, 1e-6f)
    val innerH = (height - 2f * pad).coerceAtLeast(1f)
    val innerW = (width - 2f * pad).coerceAtLeast(1f)
    val n = samples.size
    val points = List(n) { i ->
        val t = i / (n - 1).toFloat()
        val x = pad + t * innerW
        val v = samples[i]
        val ny = (v - minV) / span
        val y = pad + innerH * (1f - ny)
        Offset(x, y)
    }
    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val dx = (p1.x - p0.x) / 3f
        path.cubicTo(
            p0.x + dx,
            p0.y,
            p1.x - dx,
            p1.y,
            p1.x,
            p1.y
        )
    }
    return path
}
