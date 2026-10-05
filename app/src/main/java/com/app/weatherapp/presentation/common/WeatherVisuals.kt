package com.app.weatherapp.presentation.common

import androidx.annotation.StringRes
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.WeatherCondition

/** Which glyph to draw. Rendering lives in ui/components/WeatherIcon.kt. */
enum class WeatherIconType {
    SUN, MOON, PARTLY_CLOUDY_DAY, PARTLY_CLOUDY_NIGHT, CLOUD, FOG, DRIZZLE, RAIN, HEAVY_RAIN,
    SLEET, SNOW, THUNDERSTORM, WIND, SUNRISE, SUNSET,
}

/** Background mood. Each style defines its own light and dark gradients in ui/theme/WeatherGradients.kt. */
enum class WeatherBackgroundStyle {
    CLEAR_DAY, CLOUDY_DAY, RAIN, STORM, SNOW, FOG, CLEAR_NIGHT, CLOUDY_NIGHT,
}

/**
 * WeatherCondition -> icon / background / description. Kept out of Composables so it can be
 * unit-tested and reused by every screen.
 */
object WeatherVisualMapper {

    /** Beaufort 6 ("strong breeze"): from here wind is the most notable thing about dry weather. */
    private const val WINDY_FROM_KMH = 39.0

    fun icon(condition: WeatherCondition, isDay: Boolean, windSpeedKmh: Double? = null): WeatherIconType {
        val isDry = condition in setOf(
            WeatherCondition.CLEAR,
            WeatherCondition.MAINLY_CLEAR,
            WeatherCondition.PARTLY_CLOUDY,
            WeatherCondition.CLOUDY,
        )
        if (isDry && windSpeedKmh != null && windSpeedKmh >= WINDY_FROM_KMH) return WeatherIconType.WIND

        return when (condition) {
            WeatherCondition.CLEAR -> if (isDay) WeatherIconType.SUN else WeatherIconType.MOON
            WeatherCondition.MAINLY_CLEAR,
            WeatherCondition.PARTLY_CLOUDY ->
                if (isDay) WeatherIconType.PARTLY_CLOUDY_DAY else WeatherIconType.PARTLY_CLOUDY_NIGHT
            WeatherCondition.CLOUDY, WeatherCondition.UNKNOWN -> WeatherIconType.CLOUD
            WeatherCondition.FOG -> WeatherIconType.FOG
            WeatherCondition.DRIZZLE -> WeatherIconType.DRIZZLE
            WeatherCondition.RAIN -> WeatherIconType.RAIN
            WeatherCondition.HEAVY_RAIN -> WeatherIconType.HEAVY_RAIN
            WeatherCondition.FREEZING_RAIN -> WeatherIconType.SLEET
            WeatherCondition.SNOW, WeatherCondition.HEAVY_SNOW -> WeatherIconType.SNOW
            WeatherCondition.THUNDERSTORM -> WeatherIconType.THUNDERSTORM
        }
    }

    fun background(condition: WeatherCondition, isDay: Boolean): WeatherBackgroundStyle = when (condition) {
        WeatherCondition.CLEAR,
        WeatherCondition.MAINLY_CLEAR ->
            if (isDay) WeatherBackgroundStyle.CLEAR_DAY else WeatherBackgroundStyle.CLEAR_NIGHT
        WeatherCondition.PARTLY_CLOUDY,
        WeatherCondition.CLOUDY,
        WeatherCondition.UNKNOWN ->
            if (isDay) WeatherBackgroundStyle.CLOUDY_DAY else WeatherBackgroundStyle.CLOUDY_NIGHT
        WeatherCondition.FOG -> if (isDay) WeatherBackgroundStyle.FOG else WeatherBackgroundStyle.CLOUDY_NIGHT
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN,
        WeatherCondition.FREEZING_RAIN -> WeatherBackgroundStyle.RAIN
        WeatherCondition.SNOW,
        WeatherCondition.HEAVY_SNOW -> WeatherBackgroundStyle.SNOW
        WeatherCondition.THUNDERSTORM -> WeatherBackgroundStyle.STORM
    }

    /** Text for a condition; when the icon says "windy", the description says so too. */
    @StringRes
    fun description(
        condition: WeatherCondition,
        isDay: Boolean = true,
        icon: WeatherIconType? = null,
    ): Int = if (icon == WeatherIconType.WIND) R.string.condition_windy else when (condition) {
        WeatherCondition.CLEAR -> if (isDay) R.string.condition_sunny else R.string.condition_clear
        WeatherCondition.MAINLY_CLEAR -> if (isDay) R.string.condition_mostly_sunny else R.string.condition_mostly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.condition_partly_cloudy
        WeatherCondition.CLOUDY -> R.string.condition_cloudy
        WeatherCondition.FOG -> R.string.condition_fog
        WeatherCondition.DRIZZLE -> R.string.condition_drizzle
        WeatherCondition.RAIN -> R.string.condition_rain
        WeatherCondition.HEAVY_RAIN -> R.string.condition_heavy_rain
        WeatherCondition.FREEZING_RAIN -> R.string.condition_freezing_rain
        WeatherCondition.SNOW -> R.string.condition_snow
        WeatherCondition.HEAVY_SNOW -> R.string.condition_heavy_snow
        WeatherCondition.THUNDERSTORM -> R.string.condition_thunderstorm
        WeatherCondition.UNKNOWN -> R.string.condition_unknown
    }
}
