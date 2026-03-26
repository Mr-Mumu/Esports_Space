package com.esports.space.performance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.esports.space.data.db.entity.DeviceSnapshotEntity
import com.esports.space.performance.data.DeviceMetrics
import com.esports.space.ui.component.EcgBackground
import com.esports.space.ui.component.GlassCard
import com.esports.space.ui.theme.LocalThemeConfig
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val snapshotTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
fun PerformanceDetailScreen(
    viewModel: PerformanceViewModel,
    onBack: () -> Unit,
    onTuneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("← 返回", color = theme.primaryAccent, fontSize = 14.sp)
                }
                TextButton(onClick = onTuneClick) {
                    Text("性能调节 →", color = theme.primaryAccent, fontSize = 13.sp)
                }
            }
        }

        item {
            Text(
                "性能详情",
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            MetricsSummaryCard(metrics = uiState.currentMetrics)
        }

        item {
            Text(
                "CPU / GPU 频率趋势",
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 4.dp)
                            .background(theme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CPU", color = theme.textPrimary, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 4.dp)
                            .background(theme.secondaryAccent.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GPU", color = theme.textPrimary, fontSize = 11.sp)
                }
            }
        }

        item {
            EcgBackground(
                cpuFreqHistory = uiState.cpuFreqHistory,
                gpuFreqHistory = uiState.gpuFreqHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }

        item {
            Text(
                "近 1 小时快照",
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(
            items = uiState.recentSnapshots,
            key = { it.id }
        ) { snapshot ->
            SnapshotRow(snapshot = snapshot)
        }
    }
}

@Composable
private fun MetricsSummaryCard(metrics: DeviceMetrics?) {
    val theme = LocalThemeConfig.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("当前状态", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            SummaryLine(
                "CPU",
                metrics?.cpuTempCelsius?.let { "%.1f°C".format(it) } ?: "—",
                metrics?.cpuFreqMhz?.let { "${it} MHz" } ?: "—"
            )
            SummaryLine(
                "GPU",
                metrics?.gpuTempCelsius?.let { "%.1f°C".format(it) } ?: "—",
                metrics?.gpuFreqMhz?.let { "${it} MHz" } ?: "—"
            )
            SummaryLine(
                "RAM",
                "${metrics?.ramUsedMb ?: 0} / ${metrics?.ramTotalMb ?: 0} MB",
                metrics?.let { m ->
                    if (m.ramTotalMb > 0) "%.0f%%".format(m.ramUsedMb * 100f / m.ramTotalMb) else null
                }
            )
            SummaryLine(
                "电量",
                metrics?.batteryPercent?.let { "$it%" } ?: "—",
                metrics?.batteryTemperature?.let { "%.1f°C".format(it) }
            )
            if (metrics?.networkLatencyMs != null) {
                SummaryLine("网络延迟", "${metrics.networkLatencyMs} ms", null)
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, primary: String, secondary: String?) {
    val theme = LocalThemeConfig.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = theme.textSecondary, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(primary, color = theme.textPrimary, fontSize = 13.sp)
            if (secondary != null) {
                Text(secondary, color = theme.textSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SnapshotRow(snapshot: DeviceSnapshotEntity) {
    val theme = LocalThemeConfig.current
    val timeText = snapshotTimeFormatter.format(Instant.ofEpochMilli(snapshot.timestamp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(timeText, color = theme.primaryAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                buildString {
                    append("CPU ")
                    append(snapshot.cpuTemp?.let { "%.1f°C".format(it) } ?: "—")
                    append(" · ")
                    append(snapshot.cpuFreqMhz?.let { "${it}MHz" } ?: "—")
                },
                color = theme.textPrimary,
                fontSize = 11.sp
            )
            Text(
                buildString {
                    append("GPU ")
                    append(snapshot.gpuTemp?.let { "%.1f°C".format(it) } ?: "—")
                    append(" · ")
                    append(snapshot.gpuFreqMhz?.let { "${it}MHz" } ?: "—")
                },
                color = theme.textPrimary,
                fontSize = 11.sp
            )
            Text(
                "RAM ${"%.0f".format(snapshot.ramUsagePercent)}% · 电量 ${snapshot.batteryPercent}% · 延迟 ${
                    if (snapshot.networkLatencyMs >= 0) "${snapshot.networkLatencyMs}ms" else "—"
                }",
                color = theme.textSecondary,
                fontSize = 10.sp
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = theme.textSecondary.copy(alpha = 0.15f)
            )
        }
    }
}
