package com.app.weatherapp.domain.repository

import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.model.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    /**
     * Loads the forecast. Returns a recent in-memory result when [forceRefresh] is false,
     * and falls back to the persisted cache when the network fails.
     */
    suspend fun getWeather(location: WeatherLocation, forceRefresh: Boolean = false): WeatherResult

    /** Last successfully downloaded forecast, without touching the network. */
    suspend fun getCachedWeather(locationId: String): WeatherResult?
}

interface CitySearchRepository {
    /** Returns [com.app.weatherapp.domain.model.DataError.CityNotFound] when nothing matches. */
    suspend fun search(query: String): DataResult<List<WeatherLocation>>

    companion object {
        /** Geocoding returns nothing useful for a single character. */
        const val MIN_QUERY_LENGTH = 2
    }
}

interface SavedLocationsRepository {
    /** Saved cities in the order the user added them (current location is not part of this list). */
    val savedCities: Flow<List<WeatherLocation>>

    /** Whether the device location should be shown as the first location. */
    val isCurrentLocationEnabled: Flow<Boolean>

    /** Last resolved device location, so it can be shown offline and in the list. */
    val lastKnownCurrentLocation: Flow<WeatherLocation?>

    /** Id of the location shown on the home screen, or null when nothing has been chosen yet. */
    val selectedLocationId: Flow<String?>

    suspend fun addCity(location: WeatherLocation)
    suspend fun removeCity(locationId: String)
    suspend fun setCurrentLocationEnabled(enabled: Boolean)
    suspend fun saveCurrentLocation(location: WeatherLocation)
    suspend fun selectLocation(locationId: String)
}

interface DeviceLocationRepository {
    fun hasLocationPermission(): Boolean

    /** Resolves the device position and a human-readable place name. */
    suspend fun getCurrentLocation(): DataResult<WeatherLocation>
}
