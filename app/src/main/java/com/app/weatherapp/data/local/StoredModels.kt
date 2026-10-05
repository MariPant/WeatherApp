package com.app.weatherapp.data.local

import com.app.weatherapp.data.remote.dto.ForecastDto
import com.app.weatherapp.domain.model.WeatherLocation
import kotlinx.serialization.Serializable

/** Persisted form of [WeatherLocation]. Kept separate so domain models stay free of serialization. */
@Serializable
data class StoredLocation(
    val id: String,
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class CachedForecast(
    val forecast: ForecastDto,
    val fetchedAtEpochMillis: Long,
)

fun StoredLocation.toDomain() = WeatherLocation(
    id = id,
    name = name,
    region = region,
    country = country,
    countryCode = countryCode,
    latitude = latitude,
    longitude = longitude,
)

fun WeatherLocation.toStored() = StoredLocation(
    id = id,
    name = name,
    region = region,
    country = country,
    countryCode = countryCode,
    latitude = latitude,
    longitude = longitude,
)
