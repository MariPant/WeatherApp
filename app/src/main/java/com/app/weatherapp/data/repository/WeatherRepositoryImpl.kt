package com.app.weatherapp.data.repository

import com.app.weatherapp.data.local.CachedForecast
import com.app.weatherapp.data.local.WeatherCacheDataSource
import com.app.weatherapp.data.mapper.ForecastMapper
import com.app.weatherapp.data.remote.ForecastApi
import com.app.weatherapp.data.remote.safeApiCall
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.Weather
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.model.WeatherResult
import com.app.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

/**
 * Network first, with two cache levels:
 * - in-memory, so switching between cities does not refetch data that is a few minutes old;
 * - DataStore, so the last successful forecast is available offline.
 */
class WeatherRepositoryImpl(
    private val api: ForecastApi,
    private val cache: WeatherCacheDataSource,
    private val clock: () -> Instant = Instant::now,
) : WeatherRepository {

    private data class MemoryEntry(val latitude: Double, val longitude: Double, val weather: Weather)

    private val memoryLock = Mutex()
    private val memory = mutableMapOf<String, MemoryEntry>()

    override suspend fun getWeather(location: WeatherLocation, forceRefresh: Boolean): WeatherResult {
        if (!forceRefresh) {
            recentFromMemory(location)?.let { return WeatherResult.Fresh(it) }
        }

        val fetchedAt = clock()
        val networkResult = safeApiCall { api.getForecast(location.latitude, location.longitude) }
        val error = when (networkResult) {
            is DataResult.Success -> {
                val weather = ForecastMapper.toDomain(networkResult.data, fetchedAt)
                if (weather != null) {
                    remember(location, weather)
                    persist(location.id, CachedForecast(networkResult.data, fetchedAt.toEpochMilli()))
                    return WeatherResult.Fresh(weather)
                }
                DataError.Server(null)
            }
            is DataResult.Failure -> networkResult.error
        }

        val cached = readPersisted(location.id)
        return if (cached != null) WeatherResult.Cached(cached, error) else WeatherResult.Failure(error)
    }

    override suspend fun getCachedWeather(locationId: String): WeatherResult? {
        val inMemory = memoryLock.withLock { memory[locationId]?.weather }
        if (inMemory != null) return WeatherResult.Fresh(inMemory)
        return readPersisted(locationId)?.let { WeatherResult.Cached(it, DataError.NoInternet) }
    }

    private suspend fun recentFromMemory(location: WeatherLocation): Weather? = memoryLock.withLock {
        val entry = memory[location.id] ?: return@withLock null
        val isSamePlace = abs(entry.latitude - location.latitude) < SAME_PLACE_DEGREES &&
            abs(entry.longitude - location.longitude) < SAME_PLACE_DEGREES
        val isRecent = Duration.between(entry.weather.fetchedAt, clock()) < MEMORY_TTL
        entry.weather.takeIf { isSamePlace && isRecent }
    }

    private suspend fun remember(location: WeatherLocation, weather: Weather) = memoryLock.withLock {
        memory[location.id] = MemoryEntry(location.latitude, location.longitude, weather)
    }

    private suspend fun persist(locationId: String, forecast: CachedForecast) {
        try {
            cache.write(locationId, forecast)
        } catch (e: IOException) {
            // Failing to write the offline cache must not hide fresh data from the user.
        }
    }

    private suspend fun readPersisted(locationId: String): Weather? {
        val cached = cache.read(locationId) ?: return null
        return ForecastMapper.toDomain(cached.forecast, Instant.ofEpochMilli(cached.fetchedAtEpochMillis))
    }

    private companion object {
        val MEMORY_TTL: Duration = Duration.ofMinutes(10)
        const val SAME_PLACE_DEGREES = 0.01
    }
}
