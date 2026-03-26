package com.esports.space.ui.component

import android.content.Context
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.space.ui.theme.LocalThemeConfig
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun StatusBar(modifier: Modifier = Modifier) {
    val theme = LocalThemeConfig.current
    val color = theme.textSecondary
    val context = LocalContext.current
    var timeText by remember { mutableStateOf(currentTimeString()) }
    LaunchedEffect(Unit) {
        while (true) {
            val msUntilNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
            delay(msUntilNextMinute)
            timeText = currentTimeString()
        }
    }
    val batteryPercent = remember(context) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }
    val batteryLabel =
        if (batteryPercent in 0..100) "$batteryPercent%" else "—"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeText,
            color = color,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = "Wi‑Fi",
                tint = color
            )
            Text(
                text = batteryLabel,
                color = color,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

private fun currentTimeString(): String = LocalTime.now().format(timeFormatter)
