package com.app.weatherapp.domain.model

import java.time.Duration
import java.time.LocalDateTime

/** WHO UV index categories. */
enum class UvLevel {
    LOW, MODERATE, HIGH, VERY_HIGH, EXTREME;

    companion object {
        private const val MODERATE_FROM = 3.0
        private const val HIGH_FROM = 6.0
        private const val VERY_HIGH_FROM = 8.0
        private const val EXTREME_FROM = 11.0

        fun from(index: Double): UvLevel = when {
            index < MODERATE_FROM -> LOW
            index < HIGH_FROM -> MODERATE
            index < VERY_HIGH_FROM -> HIGH
            index < EXTREME_FROM -> VERY_HIGH
            else -> EXTREME
        }
    }
}

enum class VisibilityLevel {
    VERY_POOR, POOR, MODERATE, GOOD, EXCELLENT;

    companion object {
        private const val POOR_FROM_METERS = 1_000.0
        private const val MODERATE_FROM_METERS = 4_000.0
        private const val GOOD_FROM_METERS = 10_000.0
        private const val EXCELLENT_FROM_METERS = 20_000.0

        fun from(meters: Double): VisibilityLevel = when {
            meters < POOR_FROM_METERS -> VERY_POOR
            meters < MODERATE_FROM_METERS -> POOR
            meters < GOOD_FROM_METERS -> MODERATE
            meters < EXCELLENT_FROM_METERS -> GOOD
            else -> EXCELLENT
        }
    }
}

enum class FeelsLike {
    SIMILAR, COLDER, WARMER;

    companion object {
        private const val NOTICEABLE_DIFFERENCE = 1.5

        fun compare(actual: Double, apparent: Double): FeelsLike = when {
            apparent < actual - NOTICEABLE_DIFFERENCE -> COLDER
            apparent > actual + NOTICEABLE_DIFFERENCE -> WARMER
            else -> SIMILAR
        }
    }
}

object SunProgress {
    /** Position of the sun between sunrise (0) and sunset (1), or null at night. */
    fun fraction(now: LocalDateTime, sunrise: LocalDateTime, sunset: LocalDateTime): Float? {
        if (!sunset.isAfter(sunrise) || now.isBefore(sunrise) || now.isAfter(sunset)) return null
        val total = Duration.between(sunrise, sunset).seconds.toFloat()
        val passed = Duration.between(sunrise, now).seconds.toFloat()
        return (passed / total).coerceIn(0f, 1f)
    }
}

/** Sea-level pressure relative to the standard atmosphere (1013 hPa). */
enum class PressureLevel {
    LOW, NORMAL, HIGH;

    companion object {
        private const val LOW_BELOW_HPA = 1_000.0
        private const val HIGH_ABOVE_HPA = 1_025.0

        fun from(hpa: Double): PressureLevel = when {
            hpa < LOW_BELOW_HPA -> LOW
            hpa > HIGH_ABOVE_HPA -> HIGH
            else -> NORMAL
        }
    }
}
