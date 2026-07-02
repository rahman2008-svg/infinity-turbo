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
    primary = SophisticatedPrimary,
    secondary = SophisticatedAccent,
    background = SophisticatedDarkBg,
    surface = SophisticatedDarkSurface,
    onPrimary = SophisticatedOnPrimary,
    onSecondary = SophisticatedDarkBg,
    onBackground = SophisticatedTextPrimary,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedDarkSecondarySurface,
    onSurfaceVariant = SophisticatedTextMuted,
    outline = SophisticatedBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SophisticatedOnPrimary,
    secondary = SophisticatedAccent,
    background = Color(0xFFF4F5F6),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F1113),
    onBackground = Color(0xFF0F1113),
    onSurface = Color(0xFF0F1113),
    surfaceVariant = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFF606266),
    outline = Color(0xFFD2D2D6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to enforce our elegant brand colors instead of default system colors
    content: @Composable () -> Unit,
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
