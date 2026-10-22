package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioAmber,
    onPrimary = Color.Black,
    primaryContainer = StudioCard,
    onPrimaryContainer = Color.White,
    secondary = StudioAmberLight,
    onSecondary = Color.Black,
    background = StudioDark,
    onBackground = TextLightPrimary,
    surface = StudioSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = TextLightSecondary,
    error = StatusBookedRed,
    onError = Color.White
)

@Composable
fun StudioExpoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
