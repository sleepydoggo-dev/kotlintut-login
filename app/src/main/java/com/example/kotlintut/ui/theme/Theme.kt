package com.example.kotlintut.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = RedDark,
    tertiary = Color.Gray,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE1E1E1),
    onSurface = Color(0xFFE1E1E1),
    secondaryContainer = Color(0xFF2D2D2D),
    onSecondaryContainer = Color(0xFFE1E1E1),
    primaryContainer = Color(0xFF3D0000),
    onPrimaryContainer = Color(0xFFFFDAD4)
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    secondary = RedDark,
    tertiary = Color.Gray,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF3D0000)
)

@Composable
fun RistoranteTotemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.example.kotlintut.ui.theme.Typography,
        content = content
    )
}
