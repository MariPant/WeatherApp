package com.app.weatherapp.domain

import com.app.weatherapp.domain.mapper.WeatherCodeMapper
import com.app.weatherapp.domain.mapper.WindDirectionMapper
import com.app.weatherapp.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {

    @Test
    fun mapsWmoCodesToConditions() {
        assertEquals(WeatherCondition.CLEAR, WeatherCodeMapper.toCondition(0))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCodeMapper.toCondition(2))
        assertEquals(WeatherCondition.FOG, WeatherCodeMapper.toCondition(48))
        assertEquals(WeatherCondition.FREEZING_RAIN, WeatherCodeMapper.toCondition(66))
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherCodeMapper.toCondition(82))
        assertEquals(WeatherCondition.HEAVY_SNOW, WeatherCodeMapper.toCondition(75))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCodeMapper.toCondition(99))
    }

    @Test
    fun unknownOrMissingCodeIsUnknown() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.toCondition(null))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.toCondition(42))
    }

    @Test
    fun windDegreesToCompassPoint() {
        assertEquals("N", WindDirectionMapper.toCompassPoint(0))
        assertEquals("N", WindDirectionMapper.toCompassPoint(350))
        assertEquals("NE", WindDirectionMapper.toCompassPoint(45))
        assertEquals("SW", WindDirectionMapper.toCompassPoint(225))
        assertEquals("NW", WindDirectionMapper.toCompassPoint(300))
        assertEquals("N", WindDirectionMapper.toCompassPoint(-10))
    }
}
