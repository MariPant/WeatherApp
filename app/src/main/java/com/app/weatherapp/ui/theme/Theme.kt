package com.app.weatherapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = OnSkyBlueContainer,
    secondary = Slate500,
    onSecondary = Color.White,
    background = Cloud50,
    onBackground = Slate900,
    surface = Cloud50,
    onSurface = Slate900,
    surfaceVariant = Cloud100,
    onSurfaceVariant = Slate500,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Cloud100,
    surfaceContainerHighest = Cloud200,
    outline = Cloud200,
    outlineVariant = Cloud100,
    error = ErrorLight,
)

private val DarkColors = darkColorScheme(
    primary = Moonlight,
    onPrimary = NightSky,
    primaryContainer = MoonlightContainer,
    onPrimaryContainer = OnMoonlightContainer,
    secondary = Mist400,
    onSecondary = NightSky,
    background = NightSky,
    onBackground = Mist200,
    surface = NightSky,
    onSurface = Mist200,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = Mist400,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceHigh,
    surfaceContainerHighest = NightOutline,
    outline = NightOutline,
    outlineVariant = NightSurfaceHigh,
    error = ErrorDark,
)

/**
 * App theme with a hand-tuned light and dark palette. Dynamic colour is intentionally not used:
 * the weather gradients define the brand, and wallpaper colours would clash with them.
 */
@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
