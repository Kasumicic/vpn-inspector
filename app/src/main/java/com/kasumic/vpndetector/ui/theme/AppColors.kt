package com.kasumic.vpndetector.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val primaryAction: Color,
    val primaryActionText: Color,
    val secondaryActionText: Color,
    val border: Color,
    val success: Color = Color(0xFF4ADE80),
    val error: Color = Color(0xFFF87171),
    val warning: Color = Color(0xFFFBBF24),
    val navSelectedIcon: Color,
    val navSelectedText: Color,
    val navIndicator: Color,
    val navUnselected: Color
)

val DarkAppColors = AppColors(
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    primaryText = Color(0xFFE6E1E5),
    secondaryText = Color(0xFFCAC4D0),
    primaryAction = Color(0xFFD0BCFF),
    primaryActionText = Color(0xFF381E72),
    secondaryActionText = Color(0xFFD0BCFF),
    border = Color(0xFF4A4458),
    navSelectedIcon = Color(0xFF381E72),
    navSelectedText = Color(0xFFE6E1E5),
    navIndicator = Color(0xFFE8DEF8),
    navUnselected = Color(0xFFCAC4D0)
)

val LightAppColors = AppColors(
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFF3EDF7),
    primaryText = Color(0xFF1D1B20),
    secondaryText = Color(0xFF49454F),
    primaryAction = Color(0xFF6750A4),
    primaryActionText = Color(0xFFFFFFFF),
    secondaryActionText = Color(0xFF6750A4),
    border = Color(0xFFE7E0EC),
    success = Color(0xFF16A34A),
    error = Color(0xFFDC2626),
    warning = Color(0xFFD97706),
    navSelectedIcon = Color(0xFFFFFFFF),
    navSelectedText = Color(0xFF1D1B20),
    navIndicator = Color(0xFF6750A4),
    navUnselected = Color(0xFF49454F)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
