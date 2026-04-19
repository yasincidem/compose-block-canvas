package com.yasincidem.blockcanvas.demo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Blue = Color(0xFF5B8DEF)
private val BlueContainer = Color(0xFF1A3A6E)
private val OnBlueContainer = Color(0xFFD0E4FF)

private val DarkScheme = darkColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnBlueContainer,
    background = Color(0xFF111118),
    onBackground = Color(0xFFE4E4EF),
    surface = Color(0xFF1A1A24),
    onSurface = Color(0xFFE4E4EF),
    surfaceVariant = Color(0xFF22222E),
    onSurfaceVariant = Color(0xFFAAAAAF),
    outline = Color(0xFF44444E),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2A5DC8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF0A2D6E),
    background = Color(0xFFF5F5FA),
    onBackground = Color(0xFF111118),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111118),
    surfaceVariant = Color(0xFFEEEEF4),
    onSurfaceVariant = Color(0xFF55555E),
    outline = Color(0xFFBBBBC4),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
