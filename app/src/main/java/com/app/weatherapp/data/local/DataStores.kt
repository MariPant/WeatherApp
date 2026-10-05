package com.app.weatherapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

// DataStore instances must be singletons per file, so they are declared once at top level.
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.weatherCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_cache")

/** A corrupted or unreadable file should not crash the app: treat it as empty. */
internal fun DataStore<Preferences>.safeData(): Flow<Preferences> = data.catch { error ->
    if (error is IOException) emit(emptyPreferences()) else throw error
}
