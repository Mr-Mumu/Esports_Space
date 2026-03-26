package com.esports.space.performance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.space.performance.data.DeviceMetrics
import com.esports.space.ui.component.GlassCard
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun PerformancePanel(
    metrics: DeviceMetrics?,
    onTuneClick: () -> Unit,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    GlassCard(modifier = modifier.padding(8.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("设备性能", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            MetricRow(
                "CPU",
                metrics?.cpuTempCelsius?.let { "%.1f°C".format(it) } ?: "—",
                metrics?.cpuFreqMhz?.let { "${it}MHz" } ?: "—"
            )
            MetricRow(
                "GPU",
                metrics?.gpuTempCelsius?.let { "%.1f°C".format(it) } ?: "—",
                metrics?.gpuFreqMhz?.let { "${it}MHz" } ?: "—"
            )
            MetricRow("RAM", "${metrics?.ramUsedMb ?: 0}/${metrics?.ramTotalMb ?: 0}MB", null)
            MetricRow("电量", metrics?.batteryPercent?.let { "$it%" } ?: "—", null)

            if (metrics?.networkLatencyMs != null) {
                MetricRow("延迟", "${metrics.networkLatencyMs}ms", null)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDetailClick) {
                    Text("详情 →", color = theme.primaryAccent, fontSize = 12.sp)
                }
                TextButton(onClick = onTuneClick) {
                    Text("性能调节 →", color = theme.primaryAccent, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value1: String, value2: String?) {
    val theme = LocalThemeConfig.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = theme.textSecondary, fontSize = 11.sp, modifier = Modifier.width(36.dp))
        Text(value1, color = theme.textPrimary, fontSize = 11.sp)
        if (value2 != null) {
            Text(value2, color = theme.textSecondary, fontSize = 11.sp)
        }
    }
}
