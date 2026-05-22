package com.rimapps.arqtest.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DolarGreen,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE4ECE6),
    onSurfaceVariant = Color(0xFF46524B)
)

private val DarkColorScheme = darkColorScheme(
    primary = DolarGreenDark,
    onPrimary = Color(0xFF003827),
    background = PaperDark,
    onBackground = Color(0xFFE4EEE7),
    surface = SurfaceDark,
    onSurface = Color(0xFFE4EEE7),
    surfaceVariant = Color(0xFF39463F),
    onSurfaceVariant = Color(0xFFC5D0C8)
)

@Composable
fun ArqTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = ArqTypography,
        content = content
    )
}
