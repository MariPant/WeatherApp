package com.app.weatherapp.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Complete forecast for one location. All local times are in [zoneId] (the location's own zone). */
data class Weather(
    val zoneId: ZoneId,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** When the data was downloaded from the API. Used for "Last updated" when offline. */
    val fetchedAt: Instant,
) {
    val today: DailyForecast? get() = daily.firstOrNull()
}

data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windGustsKmh: Double?,
    val windDirectionDegrees: Int,
    val pressureHpa: Double,
    val visibilityMeters: Double?,
    val uvIndex: Double?,
    val precipitationMm: Double,
    val dewPoint: Double?,
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val precipitationProbabilityPercent: Int?,
)

data class DailyForecast(
    val date: LocalDate,
    val condition: WeatherCondition,
    val minTemperature: Double,
    val maxTemperature: Double,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
    val uvIndexMax: Double?,
    val precipitationProbabilityPercent: Int?,
)
