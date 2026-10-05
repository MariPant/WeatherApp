package com.app.weatherapp.data

import com.app.weatherapp.data.mapper.ForecastMapper
import com.app.weatherapp.data.remote.dto.CurrentDto
import com.app.weatherapp.data.remote.dto.DailyDto
import com.app.weatherapp.data.remote.dto.ForecastDto
import com.app.weatherapp.data.remote.dto.HourlyDto
import com.app.weatherapp.domain.mapper.HourlyTimelineBuilder
import com.app.weatherapp.domain.model.HourlyTimelineItem
import com.app.weatherapp.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class ForecastMapperTest {

    private val hours = (0 until HOURS).map { START.plusHours(it.toLong()) }

    private fun dto(current: CurrentDto? = currentDto()) = ForecastDto(
        latitude = 60.98,
        longitude = 25.66,
        timezone = "Europe/Helsinki",
        current = current,
        hourly = HourlyDto(
            time = hours.map { it.toString() },
            // One missing value, as the API can return null for hours a model does not cover.
            temperature = hours.mapIndexed { index, _ -> if (index == NULL_HOUR) null else index.toDouble() },
            weatherCode = hours.map { RAIN_CODE },
            isDay = hours.map { if (it.hour in 8..18) 1 else 0 },
            precipitationProbability = hours.map { 40 },
            visibility = hours.mapIndexed { index, _ -> index * 1_000.0 },
            uvIndex = hours.mapIndexed { index, _ -> index / 10.0 },
            dewPoint = hours.map { 4.0 },
        ),
        daily = DailyDto(
            time = listOf("2026-10-05", "2026-10-06"),
            weatherCode = listOf(3, 95),
            temperatureMax = listOf(14.0, 12.0),
            temperatureMin = listOf(6.0, 5.0),
            sunrise = listOf("2026-10-05T07:31", "2026-10-06T07:33"),
            sunset = listOf("2026-10-05T18:40", "2026-10-06T18:37"),
        ),
    )

    private fun currentDto() = CurrentDto(
        time = "2026-10-05T08:15",
        temperature = 11.6,
        apparentTemperature = 8.9,
        humidity = 81,
        isDay = 1,
        weatherCode = 2,
        pressureMsl = 1012.4,
        windSpeed = 14.0,
        windDirection = 300,
    )

    @Test
    fun mapsCurrentConditionsAndPicksHourlyOnlyValuesForTheCurrentHour() {
        val weather = ForecastMapper.toDomain(dto(), Instant.EPOCH)!!
        assertEquals(11.6, weather.current.temperature, DELTA)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, weather.current.condition)
        assertEquals(0.8, weather.current.uvIndex!!, DELTA)
        assertEquals(8_000.0, weather.current.visibilityMeters!!, DELTA)
        assertEquals("Europe/Helsinki", weather.zoneId.id)
    }

    @Test
    fun skipsHoursWithMissingValues() {
        val weather = ForecastMapper.toDomain(dto(), Instant.EPOCH)!!
        assertEquals(HOURS - 1, weather.hourly.size)
        assertEquals(2, weather.daily.size)
    }

    @Test
    fun returnsNullWithoutCurrentBlock() {
        assertNull(ForecastMapper.toDomain(dto(current = null), Instant.EPOCH))
    }

    @Test
    fun timelineStartsNowAndContainsSunEventsInOrder() {
        val weather = ForecastMapper.toDomain(dto(), Instant.EPOCH)!!
        val timeline = HourlyTimelineBuilder.build(weather)

        val first = timeline.first() as HourlyTimelineItem.Hour
        assertTrue(first.isNow)
        assertEquals(8, first.time.hour)
        assertTrue(timeline.zipWithNext().all { (a, b) -> !b.time.isBefore(a.time) })
        assertNotNull(timeline.firstOrNull { it is HourlyTimelineItem.Sunset })
        assertNotNull(timeline.firstOrNull { it is HourlyTimelineItem.Sunrise })
    }

    private companion object {
        val START: LocalDateTime = LocalDateTime.parse("2026-10-05T00:00")
        const val HOURS = 48
        const val NULL_HOUR = 30
        const val RAIN_CODE = 61
        const val DELTA = 0.0001
    }
}
