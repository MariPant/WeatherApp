package com.app.weatherapp.domain.mapper

import com.app.weatherapp.domain.model.HourlyTimelineItem
import com.app.weatherapp.domain.model.Weather
import java.time.temporal.ChronoUnit

/** Builds the next-24-hours strip, inserting sunrise and sunset markers at the right positions. */
object HourlyTimelineBuilder {

    private const val HOURS_TO_SHOW = 24L

    fun build(weather: Weather): List<HourlyTimelineItem> {
        val nowHour = weather.current.time.truncatedTo(ChronoUnit.HOURS)
        val end = nowHour.plusHours(HOURS_TO_SHOW)

        val hours = weather.hourly
            .filter { !it.time.isBefore(nowHour) && it.time.isBefore(end) }
            .map { HourlyTimelineItem.Hour(it, isNow = it.time == nowHour) }

        val sunEvents = weather.daily.flatMap { day ->
            listOfNotNull(
                day.sunrise?.let(HourlyTimelineItem::Sunrise),
                day.sunset?.let(HourlyTimelineItem::Sunset),
            )
        }.filter { it.time.isAfter(weather.current.time) && it.time.isBefore(end) }

        // Sorting is stable, so an hour and a sun event at the same minute keep hour-first order.
        return (hours + sunEvents).sortedBy { it.time }
    }
}
