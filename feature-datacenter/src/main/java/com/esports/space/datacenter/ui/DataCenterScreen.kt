package com.esports.space.datacenter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.esports.space.ui.component.GlassCard
import com.esports.space.ui.theme.LocalThemeConfig

private fun formatDurationMs(ms: Long): String {
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    return when {
        h > 0 -> "${h}小时${m}分"
        m > 0 -> "${m}分钟"
        else -> "0分钟"
    }
}

@Composable
fun DataCenterScreen(
    viewModel: DataCenterViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalThemeConfig.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            TextButton(onClick = onBack) {
                Text("← 返回", color = theme.primaryAccent, fontSize = 14.sp)
            }
        }

        Text(
            "个人数据中心",
            color = theme.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryCard(
                title = "今日游戏时长",
                value = formatDurationMs(uiState.todayMs),
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "本周游戏天数",
                value = "${uiState.weekDays} 天",
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "本月总时长",
                value = formatDurationMs(uiState.monthMs),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        SectionTitle("近 7 日游戏时长")
        PlayTimeChart(data = uiState.dailyHistory, modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.padding(8.dp))

        SectionTitle("游戏时长占比")
        GamePieChart(data = uiState.gameDistribution, modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.padding(8.dp))

        SectionTitle("游戏时段热力")
        HeatMapView(data = uiState.heatmapData, modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.padding(12.dp))

        Text(
            uiState.healthTip,
            color = theme.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val theme = LocalThemeConfig.current
    Text(
        text,
        color = theme.textPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val theme = LocalThemeConfig.current
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                color = theme.textSecondary,
                fontSize = 11.sp,
                maxLines = 2,
            )
            Text(
                value,
                color = theme.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
            )
        }
    }
}
