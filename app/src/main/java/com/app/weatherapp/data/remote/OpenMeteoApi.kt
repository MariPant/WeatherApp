package com.app.weatherapp.data.remote

import com.app.weatherapp.data.remote.dto.ForecastDto
import com.app.weatherapp.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** https://open-meteo.com/en/docs — free for non-commercial use, no API key required. */
interface ForecastApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
    ): ForecastDto

    companion object {
        const val FORECAST_DAYS = 10
        private const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,is_day,weather_code," +
                "pressure_msl,wind_speed_10m,wind_gusts_10m,wind_direction_10m,precipitation"
        private const val HOURLY_FIELDS =
            "temperature_2m,weather_code,is_day,precipitation_probability,visibility,uv_index,dew_point_2m"
        private const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max," +
                "precipitation_probability_max"
    }
}

/** https://open-meteo.com/en/docs/geocoding-api */
interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = MAX_RESULTS,
        @Query("language") language: String,
        @Query("format") format: String = "json",
    ): GeocodingResponseDto

    companion object {
        const val MAX_RESULTS = 15
    }
}
