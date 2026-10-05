package com.app.weatherapp.domain.model

import java.time.LocalDateTime

/** One entry in the horizontal hourly strip: a forecast hour or a sunrise/sunset marker. */
sealed interface HourlyTimelineItem {
    val time: LocalDateTime

    data class Hour(val forecast: HourlyForecast, val isNow: Boolean) : HourlyTimelineItem {
        override val time: LocalDateTime get() = forecast.time
    }

    data class Sunrise(override val time: LocalDateTime) : HourlyTimelineItem
    data class Sunset(override val time: LocalDateTime) : HourlyTimelineItem
}
