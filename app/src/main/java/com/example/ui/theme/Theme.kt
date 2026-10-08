package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StudioAmber,
    onPrimary = Color.Black,
    primaryContainer = StudioAmberDark,
    onPrimaryContainer = Color.White,
    secondary = StudioAmberLight,
    onSecondary = Color.Black,
    background = StudioCharcoal,
    onBackground = TextPrimaryDark,
    surface = StudioCharcoalSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioCharcoalElevated,
    onSurfaceVariant = TextSecondaryDark,
    error = StatusBookedRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = StudioAmberDark,
    onPrimary = Color.White,
    primaryContainer = StudioAmberLight,
    onPrimaryContainer = Color(0xFF4A3000),
    secondary = StudioAmber,
    onSecondary = Color.Black,
    background = StudioBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = StudioSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = StatusBookedRed,
    onError = Color.White
)

@Composable
fun StudioExpoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded studio aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
