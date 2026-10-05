package com.app.weatherapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.app.weatherapp.domain.model.WeatherLocation
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Saved cities, current-location toggle and the selected location, stored in Preferences DataStore. */
class LocationsLocalDataSource(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    val savedCities: Flow<List<StoredLocation>> = dataStore.safeData()
        .map { prefs -> decodeList(prefs[KEY_SAVED_CITIES]) }
        .distinctUntilChanged()

    val isCurrentLocationEnabled: Flow<Boolean> = dataStore.safeData()
        .map { prefs -> prefs[KEY_CURRENT_ENABLED] ?: false }
        .distinctUntilChanged()

    val lastCurrentLocation: Flow<StoredLocation?> = dataStore.safeData()
        .map { prefs -> decodeLocation(prefs[KEY_LAST_CURRENT_LOCATION]) }
        .distinctUntilChanged()

    val selectedLocationId: Flow<String?> = dataStore.safeData()
        .map { prefs -> prefs[KEY_SELECTED_ID] }
        .distinctUntilChanged()

    suspend fun upsertCity(location: StoredLocation) {
        dataStore.edit { prefs ->
            val current = decodeList(prefs[KEY_SAVED_CITIES])
            val updated = if (current.any { it.id == location.id }) {
                current.map { if (it.id == location.id) location else it }
            } else {
                current + location
            }
            prefs[KEY_SAVED_CITIES] = json.encodeToString(LIST_SERIALIZER, updated)
        }
    }

    /** Removes a city and, if it was selected, moves the selection to a remaining location. */
    suspend fun removeCity(id: String) {
        dataStore.edit { prefs ->
            val remaining = decodeList(prefs[KEY_SAVED_CITIES]).filterNot { it.id == id }
            prefs[KEY_SAVED_CITIES] = json.encodeToString(LIST_SERIALIZER, remaining)
            if (prefs[KEY_SELECTED_ID] == id) {
                val fallback = fallbackSelection(prefs, remaining)
                if (fallback != null) prefs[KEY_SELECTED_ID] = fallback else prefs.remove(KEY_SELECTED_ID)
            }
        }
    }

    suspend fun setCurrentLocationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_CURRENT_ENABLED] = enabled
            if (!enabled && prefs[KEY_SELECTED_ID] == CURRENT_ID) {
                val fallback = decodeList(prefs[KEY_SAVED_CITIES]).firstOrNull()?.id
                if (fallback != null) prefs[KEY_SELECTED_ID] = fallback else prefs.remove(KEY_SELECTED_ID)
            }
        }
    }

    suspend fun saveLastCurrentLocation(location: StoredLocation) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_CURRENT_LOCATION] = json.encodeToString(StoredLocation.serializer(), location)
        }
    }

    suspend fun setSelectedLocationId(id: String) {
        dataStore.edit { prefs -> prefs[KEY_SELECTED_ID] = id }
    }

    private fun fallbackSelection(prefs: Preferences, remaining: List<StoredLocation>): String? =
        if (prefs[KEY_CURRENT_ENABLED] == true) CURRENT_ID else remaining.firstOrNull()?.id

    private fun decodeList(raw: String?): List<StoredLocation> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(LIST_SERIALIZER, raw)
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private fun decodeLocation(raw: String?): StoredLocation? {
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString(StoredLocation.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        val KEY_SAVED_CITIES = stringPreferencesKey("saved_cities")
        val KEY_CURRENT_ENABLED = booleanPreferencesKey("current_location_enabled")
        val KEY_LAST_CURRENT_LOCATION = stringPreferencesKey("last_current_location")
        val KEY_SELECTED_ID = stringPreferencesKey("selected_location_id")
        val LIST_SERIALIZER = ListSerializer(StoredLocation.serializer())
        const val CURRENT_ID = WeatherLocation.CURRENT_LOCATION_ID
    }
}
