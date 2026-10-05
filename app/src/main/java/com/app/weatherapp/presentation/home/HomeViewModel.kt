package com.app.weatherapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.weatherapp.WeatherApplication
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.model.WeatherResult
import com.app.weatherapp.domain.repository.DeviceLocationRepository
import com.app.weatherapp.domain.repository.SavedLocationsRepository
import com.app.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the home screen should show weather for, derived from persisted preferences. */
private sealed interface Selection {
    data object None : Selection
    data object CurrentLocation : Selection
    data class City(val location: WeatherLocation) : Selection
}

class HomeViewModel(
    private val weatherRepository: WeatherRepository,
    private val savedLocationsRepository: SavedLocationsRepository,
    private val deviceLocationRepository: DeviceLocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var selection: Selection? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                savedLocationsRepository.selectedLocationId,
                savedLocationsRepository.savedCities,
                savedLocationsRepository.isCurrentLocationEnabled,
            ) { selectedId, cities, currentEnabled -> resolveSelection(selectedId, cities, currentEnabled) }
                .distinctUntilChanged()
                .collect { newSelection ->
                    selection = newSelection
                    load(newSelection, forceRefresh = false, isPullToRefresh = false)
                }
        }
    }

    /** Pull-to-refresh: keeps the current content on screen and always hits the network. */
    fun refresh() {
        selection?.let { load(it, forceRefresh = true, isPullToRefresh = true) }
    }

    /** Retry button on the error screen. */
    fun retry() {
        selection?.let { load(it, forceRefresh = true, isPullToRefresh = false) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.update { it.copy(message = DataError.LocationPermissionDenied) }
            return
        }
        viewModelScope.launch {
            savedLocationsRepository.setCurrentLocationEnabled(true)
            savedLocationsRepository.selectLocation(WeatherLocation.CURRENT_LOCATION_ID)
            // If current location was already selected the preferences do not change, so reload manually.
            if (selection == Selection.CurrentLocation) retry()
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun resolveSelection(
        selectedId: String?,
        cities: List<WeatherLocation>,
        currentEnabled: Boolean,
    ): Selection {
        if (selectedId == WeatherLocation.CURRENT_LOCATION_ID && currentEnabled) return Selection.CurrentLocation
        cities.firstOrNull { it.id == selectedId }?.let { return Selection.City(it) }
        return when {
            currentEnabled -> Selection.CurrentLocation
            cities.isNotEmpty() -> Selection.City(cities.first())
            else -> Selection.None
        }
    }

    private fun load(selection: Selection, forceRefresh: Boolean, isPullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (isPullToRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else if (!isShowing(selection)) {
                _uiState.update { it.copy(content = HomeContent.Loading, isRefreshing = false) }
            }

            when (selection) {
                Selection.None -> _uiState.update {
                    it.copy(content = HomeContent.NoLocation, isRefreshing = false)
                }
                is Selection.City -> loadWeather(selection.location, forceRefresh, isPullToRefresh)
                Selection.CurrentLocation -> loadCurrentLocation(forceRefresh, isPullToRefresh)
            }
        }
    }

    private suspend fun loadCurrentLocation(forceRefresh: Boolean, isPullToRefresh: Boolean) {
        when (val result = deviceLocationRepository.getCurrentLocation()) {
            is DataResult.Success -> {
                savedLocationsRepository.saveCurrentLocation(result.data)
                loadWeather(result.data, forceRefresh, isPullToRefresh)
            }
            is DataResult.Failure -> {
                val lastKnown = savedLocationsRepository.lastKnownCurrentLocation.first()
                if (result.error == DataError.LocationUnavailable && lastKnown != null) {
                    // GPS is off or slow: the last known place is still a good answer.
                    loadWeather(lastKnown, forceRefresh, isPullToRefresh)
                } else {
                    _uiState.update {
                        it.copy(content = HomeContent.Error(lastKnown, result.error), isRefreshing = false)
                    }
                }
            }
        }
    }

    private suspend fun loadWeather(location: WeatherLocation, forceRefresh: Boolean, isPullToRefresh: Boolean) {
        val result = weatherRepository.getWeather(location, forceRefresh)
        _uiState.update { state ->
            when (result) {
                is WeatherResult.Fresh -> state.copy(
                    content = HomeContent.Success(HomeUiMapper.map(location, result.weather, staleReason = null)),
                    isRefreshing = false,
                )
                is WeatherResult.Cached -> state.copy(
                    content = HomeContent.Success(HomeUiMapper.map(location, result.weather, result.error)),
                    isRefreshing = false,
                    message = if (isPullToRefresh) result.error else state.message,
                )
                is WeatherResult.Failure -> {
                    val keepContent = isPullToRefresh && state.content is HomeContent.Success
                    state.copy(
                        content = if (keepContent) state.content else HomeContent.Error(location, result.error),
                        isRefreshing = false,
                        message = if (keepContent) result.error else state.message,
                    )
                }
            }
        }
    }

    /** Avoids flashing the skeleton when the same place is simply being reloaded. */
    private fun isShowing(selection: Selection): Boolean {
        val shownId = (uiState.value.content as? HomeContent.Success)?.data?.location?.id ?: return false
        return when (selection) {
            Selection.None -> false
            Selection.CurrentLocation -> shownId == WeatherLocation.CURRENT_LOCATION_ID
            is Selection.City -> shownId == selection.location.id
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as WeatherApplication).container
                HomeViewModel(
                    weatherRepository = container.weatherRepository,
                    savedLocationsRepository = container.savedLocationsRepository,
                    deviceLocationRepository = container.deviceLocationRepository,
                )
            }
        }
    }
}
