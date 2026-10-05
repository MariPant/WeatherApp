package com.app.weatherapp.presentation.home

import com.app.weatherapp.domain.mapper.HourlyTimelineBuilder
import com.app.weatherapp.domain.mapper.WindDirectionMapper
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.FeelsLike
import com.app.weatherapp.domain.model.PressureLevel
import com.app.weatherapp.domain.model.SunProgress
import com.app.weatherapp.domain.model.UvLevel
import com.app.weatherapp.domain.model.VisibilityLevel
import com.app.weatherapp.domain.model.Weather
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.presentation.common.WeatherVisualMapper

/** Pure Weather -> HomeWeatherUi conversion. Runs in the ViewModel, never in Composables. */
object HomeUiMapper {

    fun map(location: WeatherLocation, weather: Weather, staleReason: DataError?): HomeWeatherUi {
        val current = weather.current
        val today = weather.today
        val icon = WeatherVisualMapper.icon(current.condition, current.isDay, current.windSpeedKmh)
        return HomeWeatherUi(
            location = location,
            weather = weather,
            icon = icon,
            description = WeatherVisualMapper.description(current.condition, current.isDay, icon),
            background = WeatherVisualMapper.background(current.condition, current.isDay),
            isNight = !current.isDay,
            staleReason = staleReason,
            hourly = HourlyTimelineBuilder.build(weather),
            daily = mapDaily(weather),
            details = DetailsUi(
                apparentTemperature = current.apparentTemperature,
                feelsLike = FeelsLike.compare(current.temperature, current.apparentTemperature),
                humidityPercent = current.humidityPercent,
                dewPoint = current.dewPoint,
                windSpeedKmh = current.windSpeedKmh,
                windGustsKmh = current.windGustsKmh,
                windDirectionDegrees = current.windDirectionDegrees,
                windCompass = WindDirectionMapper.toCompassPoint(current.windDirectionDegrees),
                uvIndex = current.uvIndex,
                uvLevel = current.uvIndex?.let(UvLevel::from),
                visibilityMeters = current.visibilityMeters,
                visibilityLevel = current.visibilityMeters?.let(VisibilityLevel::from),
                pressureHpa = current.pressureHpa,
                pressureLevel = PressureLevel.from(current.pressureHpa),
            ),
            sun = today?.let { day ->
                val sunrise = day.sunrise ?: return@let null
                val sunset = day.sunset ?: return@let null
                SunUi(sunrise, sunset, SunProgress.fraction(current.time, sunrise, sunset))
            },
        )
    }

    private fun mapDaily(weather: Weather): List<DailyRowUi> {
        val days = weather.daily
        if (days.isEmpty()) return emptyList()
        val periodMin = minOf(days.minOf { it.minTemperature }, weather.current.temperature)
        val periodMax = maxOf(days.maxOf { it.maxTemperature }, weather.current.temperature)
        val span = (periodMax - periodMin).takeIf { it > 0 } ?: 1.0
        fun position(value: Double) = ((value - periodMin) / span).toFloat().coerceIn(0f, 1f)

        return days.mapIndexed { index, day ->
            val isToday = index == 0
            DailyRowUi(
                forecast = day,
                isToday = isToday,
                icon = WeatherVisualMapper.icon(day.condition, isDay = true),
                description = WeatherVisualMapper.description(day.condition),
                rangeStart = position(day.minTemperature),
                rangeEnd = position(day.maxTemperature),
                currentMarker = if (isToday) position(weather.current.temperature) else null,
                periodMin = periodMin,
                periodMax = periodMax,
            )
        }
    }
}
