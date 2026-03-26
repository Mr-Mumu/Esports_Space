package com.esports.space.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeConfig = staticCompositionLocalOf { GalaxyThemeConfig }

@Composable
fun EsportsTheme(
    themeConfig: ThemeConfig = GalaxyThemeConfig,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        background = themeConfig.background,
        surface = themeConfig.surface,
        primary = themeConfig.primaryAccent,
        secondary = themeConfig.secondaryAccent,
        onBackground = themeConfig.textPrimary,
        onSurface = themeConfig.textSecondary,
        error = themeConfig.liveIndicator
    )
    CompositionLocalProvider(LocalThemeConfig provides themeConfig) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
