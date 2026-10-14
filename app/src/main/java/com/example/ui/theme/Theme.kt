package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NudgeDarkPrimary,
    onPrimary = NudgeDarkOnPrimary,
    primaryContainer = NudgeDarkPrimaryContainer,
    onPrimaryContainer = NudgeDarkOnPrimaryContainer,
    secondary = NudgeDarkSecondary,
    onSecondary = NudgeDarkOnSecondary,
    secondaryContainer = NudgeDarkSecondaryContainer,
    onSecondaryContainer = NudgeDarkOnSecondaryContainer,
    tertiary = NudgeDarkTertiary,
    onTertiary = NudgeDarkOnTertiary,
    tertiaryContainer = NudgeDarkTertiaryContainer,
    onTertiaryContainer = NudgeDarkOnTertiaryContainer,
    background = NudgeDarkBackground,
    onBackground = NudgeDarkOnBackground,
    surface = NudgeDarkSurface,
    onSurface = NudgeDarkOnSurface,
    surfaceVariant = NudgeDarkSurfaceVariant,
    onSurfaceVariant = NudgeDarkOnSurfaceVariant,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NudgeLightPrimary,
    onPrimary = NudgeLightOnPrimary,
    primaryContainer = NudgeLightPrimaryContainer,
    onPrimaryContainer = NudgeLightOnPrimaryContainer,
    secondary = NudgeLightSecondary,
    onSecondary = NudgeLightOnSecondary,
    secondaryContainer = NudgeLightSecondaryContainer,
    onSecondaryContainer = NudgeLightOnSecondaryContainer,
    tertiary = NudgeLightTertiary,
    onTertiary = NudgeLightOnTertiary,
    tertiaryContainer = NudgeLightTertiaryContainer,
    onTertiaryContainer = NudgeLightOnTertiaryContainer,
    background = NudgeLightBackground,
    onBackground = NudgeLightOnBackground,
    surface = NudgeLightSurface,
    onSurface = NudgeLightOnSurface,
    surfaceVariant = NudgeLightSurfaceVariant,
    onSurfaceVariant = NudgeLightOnSurfaceVariant,
  )

@Composable
fun NudgeTheme(
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

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
