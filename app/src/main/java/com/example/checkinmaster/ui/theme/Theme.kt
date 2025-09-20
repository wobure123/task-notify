package com.example.checkinmaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFFE5167),
    secondary = Color(0xFFFF9C00),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFE5167),
    secondary = Color(0xFFFF9C00)
)

@Composable
fun CheckInMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
