package com.app.weatherapp.domain.model

/**
 * App-level weather condition, independent of any API's code system.
 * UI decides how each condition looks (icon, background); the domain only decides what it is.
 */
enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    HEAVY_RAIN,
    FREEZING_RAIN,
    SNOW,
    HEAVY_SNOW,
    THUNDERSTORM,
    UNKNOWN,
}
