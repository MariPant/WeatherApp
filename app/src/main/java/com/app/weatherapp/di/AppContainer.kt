package com.app.weatherapp.di

import android.content.Context
import com.app.weatherapp.data.local.LocationsLocalDataSource
import com.app.weatherapp.data.local.WeatherCacheDataSource
import com.app.weatherapp.data.local.settingsDataStore
import com.app.weatherapp.data.local.weatherCacheDataStore
import com.app.weatherapp.data.location.DeviceLocationRepositoryImpl
import com.app.weatherapp.data.location.ReverseGeocoder
import com.app.weatherapp.data.remote.NetworkModule
import com.app.weatherapp.data.repository.CitySearchRepositoryImpl
import com.app.weatherapp.data.repository.SavedLocationsRepositoryImpl
import com.app.weatherapp.data.repository.WeatherRepositoryImpl
import com.app.weatherapp.domain.repository.CitySearchRepository
import com.app.weatherapp.domain.repository.DeviceLocationRepository
import com.app.weatherapp.domain.repository.SavedLocationsRepository
import com.app.weatherapp.domain.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

/**
 * Manual dependency injection. The app is small enough that a DI framework (Hilt/Koin)
 * would add more build complexity than it removes; swapping later only touches this file.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val network by lazy { NetworkModule(json) }
    private val weatherCache by lazy { WeatherCacheDataSource(appContext.weatherCacheDataStore, json) }
    private val locationsLocal by lazy { LocationsLocalDataSource(appContext.settingsDataStore, json) }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(network.forecastApi, weatherCache)
    }

    val citySearchRepository: CitySearchRepository by lazy {
        CitySearchRepositoryImpl(network.geocodingApi)
    }

    val savedLocationsRepository: SavedLocationsRepository by lazy {
        SavedLocationsRepositoryImpl(locationsLocal, weatherCache)
    }

    val deviceLocationRepository: DeviceLocationRepository by lazy {
        DeviceLocationRepositoryImpl(
            context = appContext,
            fusedClient = LocationServices.getFusedLocationProviderClient(appContext),
            reverseGeocoder = ReverseGeocoder(appContext, Dispatchers.IO),
        )
    }
}
