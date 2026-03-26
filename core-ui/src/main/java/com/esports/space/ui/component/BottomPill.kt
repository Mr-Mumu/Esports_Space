package com.esports.space.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun BottomPill(
    onDataCenter: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val surface = theme.surface
    val border = theme.primaryAccent
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .background(surface.copy(alpha = 0.18f), RoundedCornerShape(40.dp))
                .border(1.dp, border.copy(alpha = 0.22f), RoundedCornerShape(40.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDataCenter) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = "数据中心",
                    tint = theme.textPrimary
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = theme.textPrimary
                )
            }
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索",
                    tint = theme.textPrimary
                )
            }
        }
    }
}
