package com.app.weatherapp.presentation.locations

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.weatherapp.WeatherApplication
import com.app.weatherapp.domain.model.Weather
import com.app.weatherapp.domain.model.WeatherCondition
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.model.WeatherResult
import com.app.weatherapp.domain.repository.SavedLocationsRepository
import com.app.weatherapp.domain.repository.WeatherRepository
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.presentation.common.WeatherVisualMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

data class LocationsUiState(
    val items: List<LocationItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

data class LocationItemUi(
    val id: String,
    /** Null only for the device location before it has ever been resolved. */
    val location: WeatherLocation?,
    val isCurrentLocation: Boolean,
    val isSelected: Boolean,
    val weather: LocationWeatherUi,
)

sealed interface LocationWeatherUi {
    data object Loading : LocationWeatherUi
    data object Unavailable : LocationWeatherUi
    data class Loaded(
        val temperature: Double,
        val minTemperature: Double?,
        val maxTemperature: Double?,
        val condition: WeatherCondition,
        val isDay: Boolean,
        val icon: WeatherIconType,
        @StringRes val description: Int,
        val background: WeatherBackgroundStyle,
        val zoneId: ZoneId,
        val isStale: Boolean,
    ) : LocationWeatherUi
}

sealed interface LocationsEvent {
    data object NavigateHome : LocationsEvent
    data object LocationPermissionDenied : LocationsEvent
}

class LocationsViewModel(
    private val weatherRepository: WeatherRepository,
    private val savedLocationsRepository: SavedLocationsRepository,
) : ViewModel() {

    private data class Entry(val id: String, val location: WeatherLocation?, val isCurrent: Boolean)

    private val weatherById = MutableStateFlow<Map<String, LocationWeatherUi>>(emptyMap())
    private val isRefreshing = MutableStateFlow(false)

    /** Which coordinates each id was last loaded for, so a moved device location gets refetched. */
    private val requestedFor = mutableMapOf<String, WeatherLocation>()

    private val _events = Channel<LocationsEvent>(Channel.BUFFERED)
    val events: Flow<LocationsEvent> = _events.receiveAsFlow()

    private val entries: StateFlow<List<Entry>?> = combine(
        savedLocationsRepository.savedCities,
        savedLocationsRepository.isCurrentLocationEnabled,
        savedLocationsRepository.lastKnownCurrentLocation,
    ) { cities, currentEnabled, lastCurrent ->
        val current = if (currentEnabled) {
            listOf(Entry(WeatherLocation.CURRENT_LOCATION_ID, lastCurrent, isCurrent = true))
        } else {
            emptyList()
        }
        current + cities.map { Entry(it.id, it, isCurrent = false) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val uiState: StateFlow<LocationsUiState> = combine(
        entries.filterNotNull(),
        savedLocationsRepository.selectedLocationId,
        weatherById,
        isRefreshing,
    ) { entryList, selectedId, weather, refreshing ->
        val effectiveSelection = selectedId?.takeIf { id -> entryList.any { it.id == id } }
            ?: entryList.firstOrNull()?.id
        LocationsUiState(
            items = entryList.map { entry ->
                LocationItemUi(
                    id = entry.id,
                    location = entry.location,
                    isCurrentLocation = entry.isCurrent,
                    isSelected = entry.id == effectiveSelection,
                    weather = weather[entry.id]
                        ?: if (entry.location == null) LocationWeatherUi.Unavailable else LocationWeatherUi.Loading,
                )
            },
            isLoading = false,
            isRefreshing = refreshing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LocationsUiState())

    init {
        viewModelScope.launch {
            entries.filterNotNull().collect { list ->
                list.mapNotNull { it.location }
                    .filter { requestedFor[it.id] != it }
                    .forEach { location ->
                        requestedFor[location.id] = location
                        launch { loadWeather(location, forceRefresh = false) }
                    }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            entries.value.orEmpty()
                .mapNotNull { it.location }
                .map { location -> async { loadWeather(location, forceRefresh = true) } }
                .awaitAll()
            isRefreshing.value = false
        }
    }

    fun onLocationSelected(id: String) {
        viewModelScope.launch {
            savedLocationsRepository.selectLocation(id)
            _events.send(LocationsEvent.NavigateHome)
        }
    }

    fun onLocationRemoved(id: String) {
        viewModelScope.launch {
            if (id == WeatherLocation.CURRENT_LOCATION_ID) {
                savedLocationsRepository.setCurrentLocationEnabled(false)
            } else {
                savedLocationsRepository.removeCity(id)
            }
            requestedFor.remove(id)
            weatherById.update { it - id }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                savedLocationsRepository.setCurrentLocationEnabled(true)
                savedLocationsRepository.selectLocation(WeatherLocation.CURRENT_LOCATION_ID)
                _events.send(LocationsEvent.NavigateHome)
            } else {
                _events.send(LocationsEvent.LocationPermissionDenied)
            }
        }
    }

    private suspend fun loadWeather(location: WeatherLocation, forceRefresh: Boolean) {
        requestedFor[location.id] = location
        val state = when (val result = weatherRepository.getWeather(location, forceRefresh)) {
            is WeatherResult.Fresh -> result.weather.toUi(isStale = false)
            is WeatherResult.Cached -> result.weather.toUi(isStale = true)
            is WeatherResult.Failure -> weatherById.value[location.id]
                ?.takeIf { it is LocationWeatherUi.Loaded }
                ?: LocationWeatherUi.Unavailable
        }
        weatherById.update { it + (location.id to state) }
    }

    private fun Weather.toUi(isStale: Boolean): LocationWeatherUi.Loaded {
        val icon = WeatherVisualMapper.icon(current.condition, current.isDay, current.windSpeedKmh)
        return LocationWeatherUi.Loaded(
            temperature = current.temperature,
            minTemperature = today?.minTemperature,
            maxTemperature = today?.maxTemperature,
            condition = current.condition,
            isDay = current.isDay,
            icon = icon,
            description = WeatherVisualMapper.description(current.condition, current.isDay, icon),
            background = WeatherVisualMapper.background(current.condition, current.isDay),
            zoneId = zoneId,
            isStale = isStale,
        )
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as WeatherApplication).container
                LocationsViewModel(container.weatherRepository, container.savedLocationsRepository)
            }
        }
    }
}
