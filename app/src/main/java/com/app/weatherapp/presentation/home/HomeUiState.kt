package com.app.weatherapp.presentation.home

import androidx.annotation.StringRes
import com.app.weatherapp.domain.model.DailyForecast
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.FeelsLike
import com.app.weatherapp.domain.model.HourlyTimelineItem
import com.app.weatherapp.domain.model.PressureLevel
import com.app.weatherapp.domain.model.UvLevel
import com.app.weatherapp.domain.model.VisibilityLevel
import com.app.weatherapp.domain.model.Weather
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle
import com.app.weatherapp.presentation.common.WeatherIconType
import java.time.LocalDateTime

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    /** True while a pull-to-refresh is running on top of already visible content. */
    val isRefreshing: Boolean = false,
    /** A transient problem to show in a snackbar (e.g. refresh failed but old data is still valid). */
    val message: DataError? = null,
)

sealed interface HomeContent {
    data object Loading : HomeContent

    /** First launch: nothing saved and current location not enabled yet. */
    data object NoLocation : HomeContent

    data class Success(val data: HomeWeatherUi) : HomeContent

    data class Error(val location: WeatherLocation?, val error: DataError) : HomeContent
}

/** Everything the home screen draws, prepared by [HomeUiMapper] so Composables only lay it out. */
data class HomeWeatherUi(
    val location: WeatherLocation,
    val weather: Weather,
    val icon: WeatherIconType,
    @StringRes val description: Int,
    val background: WeatherBackgroundStyle,
    val isNight: Boolean,
    /** Non-null when the data came from the offline cache because loading failed. */
    val staleReason: DataError?,
    val hourly: List<HourlyTimelineItem>,
    val daily: List<DailyRowUi>,
    val details: DetailsUi,
    val sun: SunUi?,
)

data class DailyRowUi(
    val forecast: DailyForecast,
    val isToday: Boolean,
    val icon: WeatherIconType,
    @StringRes val description: Int,
    /** Position of this day's min/max inside the whole period's temperature span, 0..1. */
    val rangeStart: Float,
    val rangeEnd: Float,
    /** Current temperature marker on today's bar, 0..1. */
    val currentMarker: Float?,
    /** Lowest and highest temperature of the whole period; used to colour the bar. */
    val periodMin: Double,
    val periodMax: Double,
)

data class DetailsUi(
    val apparentTemperature: Double,
    val feelsLike: FeelsLike,
    val humidityPercent: Int,
    val dewPoint: Double?,
    val windSpeedKmh: Double,
    val windGustsKmh: Double?,
    val windDirectionDegrees: Int,
    val windCompass: String,
    val uvIndex: Double?,
    val uvLevel: UvLevel?,
    val visibilityMeters: Double?,
    val visibilityLevel: VisibilityLevel?,
    val pressureHpa: Double,
    val pressureLevel: PressureLevel,
)

data class SunUi(
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    /** 0..1 between sunrise and sunset, null when the sun is down. */
    val progress: Float?,
)
