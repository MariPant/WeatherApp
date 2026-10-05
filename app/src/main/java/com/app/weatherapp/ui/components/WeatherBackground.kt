package com.app.weatherapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle
import com.app.weatherapp.ui.theme.WeatherGradients

private const val GRADIENT_ANIMATION_MILLIS = 900

/** Full-screen gradient that morphs smoothly when the weather or the city changes. */
@Composable
fun WeatherBackground(
    style: WeatherBackgroundStyle,
    isNight: Boolean,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val stops = weatherGradientStops(style, isNight, darkTheme)
    val top = animateStop(stops[0], "gradientTop")
    val middle = animateStop(stops[1], "gradientMiddle")
    val bottom = animateStop(stops[2], "gradientBottom")
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top.value, middle.value, bottom.value))),
    )
}

/** Night always uses the deeper palette, even in light theme. */
fun weatherGradientStops(style: WeatherBackgroundStyle, isNight: Boolean, darkTheme: Boolean): List<Color> {
    val palette = WeatherGradients.palette(style)
    return if (darkTheme || isNight) palette.dark else palette.light
}

@Composable
private fun animateStop(target: Color, label: String): State<Color> =
    animateColorAsState(target, animationSpec = tween(GRADIENT_ANIMATION_MILLIS), label = label)
