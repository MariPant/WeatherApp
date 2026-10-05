package com.app.weatherapp.domain.mapper

import com.app.weatherapp.domain.model.WeatherCondition

/**
 * Maps WMO weather interpretation codes (used by Open-Meteo) to [WeatherCondition].
 * Based on the code table from the original WeatherWizard `weatherCodeToDescription`.
 * https://open-meteo.com/en/docs#weather_variable_documentation
 */
object WeatherCodeMapper {

    fun toCondition(code: Int?): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        1 -> WeatherCondition.MAINLY_CLEAR
        2 -> WeatherCondition.PARTLY_CLOUDY
        3 -> WeatherCondition.CLOUDY
        45, 48 -> WeatherCondition.FOG
        51, 53, 55 -> WeatherCondition.DRIZZLE
        56, 57, 66, 67 -> WeatherCondition.FREEZING_RAIN
        61, 63, 80, 81 -> WeatherCondition.RAIN
        65, 82 -> WeatherCondition.HEAVY_RAIN
        71, 73, 77, 85 -> WeatherCondition.SNOW
        75, 86 -> WeatherCondition.HEAVY_SNOW
        95, 96, 99 -> WeatherCondition.THUNDERSTORM
        else -> WeatherCondition.UNKNOWN
    }
}

/** Compass point for a wind direction in degrees, from the original WeatherWizard helper. */
object WindDirectionMapper {
    private val points = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    private const val DEGREES_PER_POINT = 45.0
    private const val FULL_CIRCLE = 360

    fun toCompassPoint(degrees: Int): String {
        val normalized = ((degrees % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE
        val index = Math.round(normalized / DEGREES_PER_POINT).toInt() % points.size
        return points[index]
    }
}
