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
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003A03),
    primaryContainer = Color(0xFF005307),
    onPrimaryContainer = Color(0xFFA5F49D),
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF243424),
    secondaryContainer = Color(0xFF3A4B39),
    onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFFFFB74D),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = Color(0xFF8C9388)
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5F49D),
    onPrimaryContainer = Color(0xFF002202),
    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF101F10),
    tertiary = GoldAccent,
    background = BackgroundLight,
    surface = CardBackground,
    surfaceVariant = Color(0xFFEFF2EE),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderLight
)

@Composable
fun AlezziGroceryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
