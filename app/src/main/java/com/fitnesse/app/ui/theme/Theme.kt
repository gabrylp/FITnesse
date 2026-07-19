package com.fitnesse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = GoldPrimaryContainer,
    onPrimaryContainer = GoldOnPrimaryContainer,
    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = GoldOnSecondaryContainer,
    background = WarmWhite,
    surface = WarmWhiteSurface,
    surfaceVariant = GoldSecondaryContainer,
    onBackground = WoodDark,
    onSurface = WoodDark,
    outline = WoodBrown,
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldOnDark,
    onPrimary = GoldOnPrimaryContainer,
    primaryContainer = GoldOnDarkContainer,
    onPrimaryContainer = GoldOnDark,
    secondary = GoldSecondary,
    onSecondary = WoodDark,
    secondaryContainer = GoldOnDarkContainer,
    onSecondaryContainer = GoldOnDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = GoldOnDark,
    onSurface = GoldOnDark,
    outline = GoldSecondary,
)

@Composable
fun FITnesseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
