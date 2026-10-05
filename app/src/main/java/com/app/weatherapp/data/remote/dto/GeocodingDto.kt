package com.app.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Open-Meteo geocoding response. `results` is absent when nothing matches. */
@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto>? = null,
)

@Serializable
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val admin1: String? = null,
)
