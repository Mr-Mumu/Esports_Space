package com.esports.space.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeConfig(
    val id: String,
    val name: String,
    val background: Color,
    val surface: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val liveIndicator: Color,
    val usesGameBackdrop: Boolean,
    val layoutMode: LayoutMode
)

enum class LayoutMode { THREE_COLUMN, STAGGERED, GALAXY_RADIAL }
