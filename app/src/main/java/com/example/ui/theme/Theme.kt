package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = EditorialNightOlive,
    onPrimary = Color(0xFF0C1F08),
    primaryContainer = EditorialOlive,
    onPrimaryContainer = EditorialOliveContainer,
    secondary = EditorialNightRust,
    onSecondary = Color(0xFF3B150A),
    secondaryContainer = EditorialRustDark,
    onSecondaryContainer = EditorialRustContainer,
    tertiary = EditorialNightOlive,
    onTertiary = Color.White,
    background = EditorialNightCanvas,
    onBackground = EditorialNightInk,
    surface = EditorialNightSurface,
    onSurface = EditorialNightInk,
    surfaceVariant = EditorialNightCard,
    onSurfaceVariant = EditorialNightInkSubtle,
    outline = EditorialNightBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EditorialOlive,
    onPrimary = Color.White,
    primaryContainer = EditorialOliveContainer,
    onPrimaryContainer = OnEditorialOliveContainer,
    secondary = EditorialRust,
    onSecondary = Color.White,
    secondaryContainer = EditorialRustContainer,
    onSecondaryContainer = OnEditorialRustContainer,
    tertiary = EditorialSlate,
    onTertiary = Color.White,
    tertiaryContainer = EditorialNeutralContainer,
    onTertiaryContainer = EditorialInk,
    background = EditorialCanvas,
    onBackground = EditorialInk,
    surface = EditorialSurface,
    onSurface = EditorialInk,
    surfaceVariant = EditorialCardBg,
    onSurfaceVariant = EditorialInkSubtle,
    outline = EditorialBorder
  )

@Composable
fun WealthHabitsTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  WealthHabitsTheme(darkTheme = darkTheme, content = content)
}
