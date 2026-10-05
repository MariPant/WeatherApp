package com.app.weatherapp.domain.model

/**
 * A place the app can show weather for.
 *
 * @param id stable identifier. [CURRENT_LOCATION_ID] for the device location, otherwise the
 * geocoding id of a city.
 */
data class WeatherLocation(
    val id: String,
    val name: String,
    val region: String?,
    val country: String?,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
) {
    val isCurrentLocation: Boolean get() = id == CURRENT_LOCATION_ID

    /** "Uusimaa, Finland" style secondary line, skipping blanks and duplicates of [name]. */
    val subtitle: String?
        get() = listOfNotNull(region, country)
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(", ")
            .ifBlank { null }

    companion object {
        const val CURRENT_LOCATION_ID = "current"

        fun cityId(geocodingId: Long): String = "city_$geocodingId"
    }
}
