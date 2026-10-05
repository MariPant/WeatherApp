package com.app.weatherapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle

/**
 * Gradient stops (top -> bottom) for each weather mood, with a separate darker set for dark theme
 * and night. Stops are chosen so white text stays readable; [cardScrimAlpha] darkens cards further
 * on the lighter palettes (snow, fog) where contrast would otherwise drop.
 */
data class WeatherPalette(
    val light: List<Color>,
    val dark: List<Color>,
    val cardScrimAlpha: Float,
)

object WeatherGradients {

    private const val SCRIM_DEFAULT = 0.18f
    private const val SCRIM_LIGHT_BACKGROUND = 0.26f

    fun palette(style: WeatherBackgroundStyle): WeatherPalette = when (style) {
        WeatherBackgroundStyle.CLEAR_DAY -> WeatherPalette(
            light = listOf(Color(0xFF1E62C4), Color(0xFF3A84DB), Color(0xFF5E9FE6)),
            dark = listOf(Color(0xFF0C2F63), Color(0xFF184C8F), Color(0xFF2A64AD)),
            cardScrimAlpha = SCRIM_DEFAULT,
        )
        WeatherBackgroundStyle.CLOUDY_DAY -> WeatherPalette(
            light = listOf(Color(0xFF4F6886), Color(0xFF6C86A4), Color(0xFF8AA1BB)),
            dark = listOf(Color(0xFF223246), Color(0xFF33465E), Color(0xFF465B75)),
            cardScrimAlpha = SCRIM_LIGHT_BACKGROUND,
        )
        WeatherBackgroundStyle.RAIN -> WeatherPalette(
            light = listOf(Color(0xFF26374F), Color(0xFF384E6A), Color(0xFF4E6884)),
            dark = listOf(Color(0xFF111A28), Color(0xFF1B2739), Color(0xFF26354B)),
            cardScrimAlpha = SCRIM_DEFAULT,
        )
        WeatherBackgroundStyle.STORM -> WeatherPalette(
            light = listOf(Color(0xFF262940), Color(0xFF363A58), Color(0xFF474C70)),
            dark = listOf(Color(0xFF121326), Color(0xFF1C1E3A), Color(0xFF282B4E)),
            cardScrimAlpha = SCRIM_DEFAULT,
        )
        WeatherBackgroundStyle.SNOW -> WeatherPalette(
            light = listOf(Color(0xFF58769A), Color(0xFF7792B0), Color(0xFF96ADC4)),
            dark = listOf(Color(0xFF263547), Color(0xFF3A4D63), Color(0xFF51667E)),
            cardScrimAlpha = SCRIM_LIGHT_BACKGROUND,
        )
        WeatherBackgroundStyle.FOG -> WeatherPalette(
            light = listOf(Color(0xFF5F6E7E), Color(0xFF7A8796), Color(0xFF95A1AD)),
            dark = listOf(Color(0xFF283039), Color(0xFF39424C), Color(0xFF4B5560)),
            cardScrimAlpha = SCRIM_LIGHT_BACKGROUND,
        )
        WeatherBackgroundStyle.CLEAR_NIGHT -> WeatherPalette(
            light = listOf(Color(0xFF0B1230), Color(0xFF1A2550), Color(0xFF2D3C73)),
            dark = listOf(Color(0xFF05081A), Color(0xFF0E1533), Color(0xFF1B2551)),
            cardScrimAlpha = SCRIM_DEFAULT,
        )
        WeatherBackgroundStyle.CLOUDY_NIGHT -> WeatherPalette(
            light = listOf(Color(0xFF141B2A), Color(0xFF222C40), Color(0xFF334058)),
            dark = listOf(Color(0xFF0A0E17), Color(0xFF151C2A), Color(0xFF212A3B)),
            cardScrimAlpha = SCRIM_DEFAULT,
        )
    }
}
