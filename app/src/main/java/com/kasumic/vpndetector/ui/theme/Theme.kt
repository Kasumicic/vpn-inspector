package com.kasumic.vpndetector.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val appColors = remember(colorScheme, darkTheme, dynamicColor) {
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      AppColors(
        background = colorScheme.background,
        surface = colorScheme.surfaceVariant,
        primaryText = colorScheme.onBackground,
        secondaryText = colorScheme.onSurfaceVariant,
        primaryAction = colorScheme.primary,
        primaryActionText = colorScheme.onPrimary,
        secondaryActionText = colorScheme.primary,
        border = colorScheme.outlineVariant,
        success = if (darkTheme) Color(0xFF4ADE80) else Color(0xFF16A34A),
        error = colorScheme.error,
        warning = if (darkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
        navSelectedIcon = colorScheme.onPrimaryContainer,
        navSelectedText = colorScheme.onSurface,
        navIndicator = colorScheme.primaryContainer,
        navUnselected = colorScheme.onSurfaceVariant
      )
    } else {
      if (darkTheme) DarkAppColors else LightAppColors
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    CompositionLocalProvider(
      LocalAppColors provides appColors,
      content = content
    )
  }
}

