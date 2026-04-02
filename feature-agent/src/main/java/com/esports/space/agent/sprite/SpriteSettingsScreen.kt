package com.esports.space.agent.sprite

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.esports.space.agent.ui.AgentViewModel
import com.esports.space.ui.theme.LocalThemeConfig

private data class SpritePreset(
    val id: String,
    val name: String,
    val color: Color
)

private val spritePresets = listOf(
    SpritePreset("default", "星光", Color(0xFF6C63FF)),
    SpritePreset("flame", "烈焰", Color(0xFFFF6B35)),
    SpritePreset("ice", "寒冰", Color(0xFF4FC3F7)),
    SpritePreset("forest", "翠林", Color(0xFF66BB6A)),
    SpritePreset("shadow", "暗影", Color(0xFF8E24AA)),
    SpritePreset("gold", "金辉", Color(0xFFFFD54F))
)

private val frequencySteps = listOf(15, 30, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpriteSettingsScreen(
    viewModel: AgentViewModel,
    onBack: () -> Unit
) {
    val theme = LocalThemeConfig.current
    val uiState by viewModel.uiState.collectAsState()
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setSpriteAppearance("custom:$uri")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        TopAppBar(
            title = {
                Text("智能助手设置", color = theme.textPrimary)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = theme.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            SectionHeader("总开关")
            SettingRow(
                title = "启用智能助手",
                subtitle = "开启后桌面精灵会主动推荐",
                trailing = {
                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = { viewModel.setAgentEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = theme.primaryAccent
                        )
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("外观选择")
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(spritePresets) { preset ->
                    val selected = uiState.spriteAppearance == preset.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) theme.primaryAccent.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) theme.primaryAccent
                                else theme.textSecondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSpriteAppearance(preset.id) }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(preset.color.copy(alpha = 0.8f))
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("自定义导入")
            }

            if (uiState.spriteAppearance.startsWith("custom:")) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "当前：自定义皮肤",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textSecondary
                )
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = uiState.spriteAppearance.removePrefix("custom:"),
                    contentDescription = "自定义皮肤预览",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(1.dp, theme.primaryAccent.copy(alpha = 0.45f), CircleShape)
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("AI 想法模式")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThinkingModeChip(
                    label = "本地",
                    selected = uiState.thinkingMode == "local",
                    onClick = { viewModel.setThinkingMode("local") }
                )
                ThinkingModeChip(
                    label = "云端",
                    selected = uiState.thinkingMode == "cloud",
                    onClick = { viewModel.setThinkingMode("cloud") }
                )
                ThinkingModeChip(
                    label = "混合",
                    selected = uiState.thinkingMode == "hybrid",
                    onClick = { viewModel.setThinkingMode("hybrid") }
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "推荐：混合模式（离线稳定 + 联网更有个性）",
                style = MaterialTheme.typography.bodySmall,
                color = theme.textSecondary
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("推荐频率")
            Spacer(Modifier.height(8.dp))

            val freqIndex = frequencySteps.indexOf(uiState.frequencyMinutes)
                .coerceAtLeast(0).toFloat()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${uiState.frequencyMinutes} 分钟",
                    style = MaterialTheme.typography.bodyLarge,
                    color = theme.textPrimary,
                    modifier = Modifier.width(72.dp)
                )
                Slider(
                    value = freqIndex,
                    onValueChange = { idx ->
                        val i = idx.toInt().coerceIn(0, frequencySteps.lastIndex)
                        viewModel.setFrequency(frequencySteps[i])
                    },
                    valueRange = 0f..2f,
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primaryAccent,
                        activeTrackColor = theme.primaryAccent
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("权限管理")
            Spacer(Modifier.height(8.dp))

            SettingRow(
                title = "日历读取",
                subtitle = "用于检测赛事日程冲突",
                leading = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = theme.secondaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("勿扰时段")
            Spacer(Modifier.height(8.dp))

            SettingRow(
                title = "勿扰模式",
                subtitle = "设定时间段内不推送建议",
                leading = {
                    Icon(
                        Icons.Default.DoNotDisturb,
                        contentDescription = null,
                        tint = theme.liveIndicator,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThinkingModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = null
    )
}

@Composable
private fun SectionHeader(title: String) {
    val theme = LocalThemeConfig.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = theme.primaryAccent
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val theme = LocalThemeConfig.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = theme.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = theme.textSecondary
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
