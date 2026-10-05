package com.app.weatherapp.domain

import com.app.weatherapp.domain.model.FeelsLike
import com.app.weatherapp.domain.model.PressureLevel
import com.app.weatherapp.domain.model.SunProgress
import com.app.weatherapp.domain.model.UvLevel
import com.app.weatherapp.domain.model.VisibilityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class WeatherInsightsTest {

    @Test
    fun uvLevels() {
        assertEquals(UvLevel.LOW, UvLevel.from(2.9))
        assertEquals(UvLevel.MODERATE, UvLevel.from(3.0))
        assertEquals(UvLevel.HIGH, UvLevel.from(7.0))
        assertEquals(UvLevel.VERY_HIGH, UvLevel.from(10.9))
        assertEquals(UvLevel.EXTREME, UvLevel.from(11.0))
    }

    @Test
    fun visibilityLevels() {
        assertEquals(VisibilityLevel.VERY_POOR, VisibilityLevel.from(500.0))
        assertEquals(VisibilityLevel.MODERATE, VisibilityLevel.from(5_000.0))
        assertEquals(VisibilityLevel.EXCELLENT, VisibilityLevel.from(40_000.0))
    }

    @Test
    fun feelsLikeComparison() {
        assertEquals(FeelsLike.SIMILAR, FeelsLike.compare(10.0, 9.0))
        assertEquals(FeelsLike.COLDER, FeelsLike.compare(10.0, 6.0))
        assertEquals(FeelsLike.WARMER, FeelsLike.compare(25.0, 29.0))
    }

    @Test
    fun pressureLevels() {
        assertEquals(PressureLevel.LOW, PressureLevel.from(990.0))
        assertEquals(PressureLevel.NORMAL, PressureLevel.from(1013.0))
        assertEquals(PressureLevel.HIGH, PressureLevel.from(1030.0))
    }

    @Test
    fun sunProgressIsNullAtNightAndHalfAtSolarNoon() {
        val sunrise = LocalDateTime.parse("2026-10-05T07:00")
        val sunset = LocalDateTime.parse("2026-10-05T19:00")
        assertNull(SunProgress.fraction(LocalDateTime.parse("2026-10-05T05:00"), sunrise, sunset))
        assertNull(SunProgress.fraction(LocalDateTime.parse("2026-10-05T20:00"), sunrise, sunset))
        assertEquals(0.5f, SunProgress.fraction(LocalDateTime.parse("2026-10-05T13:00"), sunrise, sunset)!!, 0.001f)
    }
}
