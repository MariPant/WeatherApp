package com.app.weatherapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.weatherapp.WeatherApplication
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.repository.CitySearchRepository
import com.app.weatherapp.domain.repository.SavedLocationsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    /** Nothing typed yet (or too short): show suggestions such as "Use current location". */
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val cities: List<WeatherLocation>) : SearchUiState
    data class NoResults(val query: String) : SearchUiState
    data class Error(val error: DataError) : SearchUiState
}

sealed interface SearchEvent {
    data object LocationChosen : SearchEvent
    data object LocationPermissionDenied : SearchEvent
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val citySearchRepository: CitySearchRepository,
    private val savedLocationsRepository: SavedLocationsRepository,
) : ViewModel() {

    private data class SearchRequest(val query: String, val attempt: Int)

    private val query = MutableStateFlow("")
    private val attempt = MutableStateFlow(0)

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    val isCurrentLocationEnabled: StateFlow<Boolean> = savedLocationsRepository.isCurrentLocationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = true)

    val uiState: StateFlow<SearchUiState> = combine(query, attempt) { text, count -> SearchRequest(text, count) }
        // Wait for a pause in typing, but react instantly when the field is cleared.
        .debounce { request -> if (request.query.isSearchable()) DEBOUNCE_MILLIS else 0L }
        .flatMapLatest { request -> searchFlow(request.query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SearchUiState.Idle)

    /** The text itself is owned by the TextField state in the UI; the ViewModel only reacts to it. */
    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun retry() {
        attempt.update { it + 1 }
    }

    fun onCitySelected(location: WeatherLocation) {
        viewModelScope.launch {
            savedLocationsRepository.addCity(location)
            savedLocationsRepository.selectLocation(location.id)
            _events.send(SearchEvent.LocationChosen)
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                savedLocationsRepository.setCurrentLocationEnabled(true)
                savedLocationsRepository.selectLocation(WeatherLocation.CURRENT_LOCATION_ID)
                _events.send(SearchEvent.LocationChosen)
            } else {
                _events.send(SearchEvent.LocationPermissionDenied)
            }
        }
    }

    private fun searchFlow(text: String): Flow<SearchUiState> = flow {
        if (!text.isSearchable()) {
            emit(SearchUiState.Idle)
            return@flow
        }
        emit(SearchUiState.Loading)
        val state = when (val result = citySearchRepository.search(text)) {
            is DataResult.Success -> SearchUiState.Results(result.data)
            is DataResult.Failure ->
                if (result.error == DataError.CityNotFound) SearchUiState.NoResults(text.trim())
                else SearchUiState.Error(result.error)
        }
        emit(state)
    }

    private fun String.isSearchable() = trim().length >= CitySearchRepository.MIN_QUERY_LENGTH

    companion object {
        private const val DEBOUNCE_MILLIS = 350L
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as WeatherApplication).container
                SearchViewModel(container.citySearchRepository, container.savedLocationsRepository)
            }
        }
    }
}
