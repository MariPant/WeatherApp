package com.app.weatherapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Stores the last successful forecast per location so the app can show something useful
 * offline. One small JSON document per location fits DataStore well; Room would be overkill.
 */
class WeatherCacheDataSource(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    suspend fun read(locationId: String): CachedForecast? {
        val raw = dataStore.safeData().first()[key(locationId)] ?: return null
        return try {
            json.decodeFromString(CachedForecast.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    suspend fun write(locationId: String, forecast: CachedForecast) {
        dataStore.edit { prefs ->
            prefs[key(locationId)] = json.encodeToString(CachedForecast.serializer(), forecast)
        }
    }

    suspend fun remove(locationId: String) {
        dataStore.edit { prefs -> prefs.remove(key(locationId)) }
    }

    private fun key(locationId: String) = stringPreferencesKey("forecast_$locationId")
}
