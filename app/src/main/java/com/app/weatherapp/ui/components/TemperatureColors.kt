package com.app.weatherapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.app.weatherapp.ui.theme.TempCold
import com.app.weatherapp.ui.theme.TempFreezing
import com.app.weatherapp.ui.theme.TempHot
import com.app.weatherapp.ui.theme.TempMild
import com.app.weatherapp.ui.theme.TempScorching
import com.app.weatherapp.ui.theme.TempWarm

/** Colour for a temperature in °C, interpolated along a cold-to-hot scale. */
object TemperatureColors {

    private val scale = listOf(
        -10.0 to TempFreezing,
        0.0 to TempCold,
        10.0 to TempMild,
        20.0 to TempWarm,
        28.0 to TempHot,
        35.0 to TempScorching,
    )

    private const val GRADIENT_SAMPLES = 6

    fun forTemperature(celsius: Double): Color {
        if (celsius <= scale.first().first) return scale.first().second
        if (celsius >= scale.last().first) return scale.last().second
        val upperIndex = scale.indexOfFirst { it.first >= celsius }
        val (lowT, lowColor) = scale[upperIndex - 1]
        val (highT, highColor) = scale[upperIndex]
        return lerp(lowColor, highColor, ((celsius - lowT) / (highT - lowT)).toFloat())
    }

    /** Evenly sampled colours between [min] and [max], for a horizontal gradient brush. */
    fun gradient(min: Double, max: Double): List<Color> =
        List(GRADIENT_SAMPLES) { index ->
            forTemperature(min + (max - min) * index / (GRADIENT_SAMPLES - 1))
        }
}
