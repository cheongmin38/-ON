package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmColorScheme = lightColorScheme(
    primary = WarmPrimary,
    onPrimary = Color.White,
    secondary = WarmSecondary,
    onSecondary = Color.White,
    tertiary = WarmTertiary,
    onTertiary = Color.White,
    background = WarmBackground,
    onBackground = WarmOnSurface,
    surface = WarmSurface,
    onSurface = WarmOnSurface,
    error = WarmAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Prioritize high-contrast, eye-safe warm lighting for elderly readers
    dynamicColor: Boolean = false, // Lock color palette for consistency across any OS version
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WarmColorScheme,
        typography = Typography,
        content = content
    )
}
