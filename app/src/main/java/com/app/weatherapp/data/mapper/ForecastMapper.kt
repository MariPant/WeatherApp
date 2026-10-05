package com.app.weatherapp.data.mapper

import com.app.weatherapp.data.remote.dto.ForecastDto
import com.app.weatherapp.data.remote.dto.GeocodingResultDto
import com.app.weatherapp.domain.mapper.WeatherCodeMapper
import com.app.weatherapp.domain.model.CurrentWeather
import com.app.weatherapp.domain.model.DailyForecast
import com.app.weatherapp.domain.model.HourlyForecast
import com.app.weatherapp.domain.model.Weather
import com.app.weatherapp.domain.model.WeatherLocation
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Converts Open-Meteo DTOs to domain models. Returns null when the response lacks the
 * minimum needed to show a forecast, so the repository can report it as a server error
 * instead of crashing on a missing field.
 */
object ForecastMapper {

    private const val IS_DAY = 1

    fun toDomain(dto: ForecastDto, fetchedAt: Instant): Weather? {
        val currentDto = dto.current ?: return null
        val currentTime = parseDateTime(currentDto.time) ?: return null
        val temperature = currentDto.temperature ?: return null
        val zoneId = resolveZone(dto)
        val hourly = mapHourly(dto)
        val currentHour = currentTime.truncatedTo(ChronoUnit.HOURS)
        val hourIndex = dto.hourly?.time?.indexOfFirst { parseDateTime(it) == currentHour } ?: -1

        val current = CurrentWeather(
            time = currentTime,
            temperature = temperature,
            apparentTemperature = currentDto.apparentTemperature ?: temperature,
            condition = WeatherCodeMapper.toCondition(currentDto.weatherCode),
            isDay = currentDto.isDay == IS_DAY,
            humidityPercent = currentDto.humidity ?: 0,
            windSpeedKmh = currentDto.windSpeed ?: 0.0,
            windGustsKmh = currentDto.windGusts,
            windDirectionDegrees = currentDto.windDirection ?: 0,
            pressureHpa = currentDto.pressureMsl ?: 0.0,
            // Visibility, UV and dew point are hourly-only variables: take the current hour.
            visibilityMeters = dto.hourly?.visibility?.getOrNull(hourIndex),
            uvIndex = dto.hourly?.uvIndex?.getOrNull(hourIndex),
            precipitationMm = currentDto.precipitation ?: 0.0,
            dewPoint = dto.hourly?.dewPoint?.getOrNull(hourIndex),
        )

        return Weather(
            zoneId = zoneId,
            current = current,
            hourly = hourly,
            daily = mapDaily(dto),
            fetchedAt = fetchedAt,
        )
    }

    fun toDomain(dto: GeocodingResultDto): WeatherLocation = WeatherLocation(
        id = WeatherLocation.cityId(dto.id),
        name = dto.name,
        region = dto.admin1,
        country = dto.country,
        countryCode = dto.countryCode,
        latitude = dto.latitude,
        longitude = dto.longitude,
    )

    private fun mapHourly(dto: ForecastDto): List<HourlyForecast> {
        val hourly = dto.hourly ?: return emptyList()
        return hourly.time.mapIndexedNotNull { index, rawTime ->
            val time = parseDateTime(rawTime) ?: return@mapIndexedNotNull null
            val temperature = hourly.temperature.getOrNull(index) ?: return@mapIndexedNotNull null
            HourlyForecast(
                time = time,
                temperature = temperature,
                condition = WeatherCodeMapper.toCondition(hourly.weatherCode.getOrNull(index)),
                isDay = hourly.isDay.getOrNull(index) == IS_DAY,
                precipitationProbabilityPercent = hourly.precipitationProbability.getOrNull(index),
            )
        }
    }

    private fun mapDaily(dto: ForecastDto): List<DailyForecast> {
        val daily = dto.daily ?: return emptyList()
        return daily.time.mapIndexedNotNull { index, rawDate ->
            val date = parseDate(rawDate) ?: return@mapIndexedNotNull null
            val min = daily.temperatureMin.getOrNull(index) ?: return@mapIndexedNotNull null
            val max = daily.temperatureMax.getOrNull(index) ?: return@mapIndexedNotNull null
            DailyForecast(
                date = date,
                condition = WeatherCodeMapper.toCondition(daily.weatherCode.getOrNull(index)),
                minTemperature = min,
                maxTemperature = max,
                sunrise = daily.sunrise.getOrNull(index)?.let(::parseDateTime),
                sunset = daily.sunset.getOrNull(index)?.let(::parseDateTime),
                uvIndexMax = daily.uvIndexMax.getOrNull(index),
                precipitationProbabilityPercent = daily.precipitationProbabilityMax.getOrNull(index),
            )
        }
    }

    private fun resolveZone(dto: ForecastDto): ZoneId = try {
        ZoneId.of(dto.timezone)
    } catch (e: DateTimeException) {
        ZoneOffset.ofTotalSeconds(dto.utcOffsetSeconds)
    }

    private fun parseDateTime(value: String): LocalDateTime? = try {
        LocalDateTime.parse(value)
    } catch (e: DateTimeParseException) {
        null
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value)
    } catch (e: DateTimeParseException) {
        null
    }
}
