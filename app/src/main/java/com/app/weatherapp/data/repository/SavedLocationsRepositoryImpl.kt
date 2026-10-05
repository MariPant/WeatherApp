package com.app.weatherapp.data.repository

import com.app.weatherapp.data.local.LocationsLocalDataSource
import com.app.weatherapp.data.local.WeatherCacheDataSource
import com.app.weatherapp.data.local.toDomain
import com.app.weatherapp.data.local.toStored
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.repository.SavedLocationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedLocationsRepositoryImpl(
    private val local: LocationsLocalDataSource,
    private val weatherCache: WeatherCacheDataSource,
) : SavedLocationsRepository {

    override val savedCities: Flow<List<WeatherLocation>> =
        local.savedCities.map { cities -> cities.map { it.toDomain() } }

    override val isCurrentLocationEnabled: Flow<Boolean> = local.isCurrentLocationEnabled

    override val lastKnownCurrentLocation: Flow<WeatherLocation?> =
        local.lastCurrentLocation.map { it?.toDomain() }

    override val selectedLocationId: Flow<String?> = local.selectedLocationId

    override suspend fun addCity(location: WeatherLocation) = local.upsertCity(location.toStored())

    override suspend fun removeCity(locationId: String) {
        local.removeCity(locationId)
        weatherCache.remove(locationId)
    }

    override suspend fun setCurrentLocationEnabled(enabled: Boolean) = local.setCurrentLocationEnabled(enabled)

    override suspend fun saveCurrentLocation(location: WeatherLocation) =
        local.saveLastCurrentLocation(location.toStored())

    override suspend fun selectLocation(locationId: String) = local.setSelectedLocationId(locationId)
}
