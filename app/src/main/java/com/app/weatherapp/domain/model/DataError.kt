package com.app.weatherapp.domain.model

/** Everything that can go wrong, expressed in terms the UI can explain to a person. */
sealed interface DataError {
    data object NoInternet : DataError
    data object Timeout : DataError
    data class Server(val httpCode: Int?) : DataError
    data object CityNotFound : DataError
    data object LocationPermissionDenied : DataError
    data object LocationUnavailable : DataError
    data object Unknown : DataError
}

/** Minimal result type so repositories never throw into ViewModels. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/**
 * Result of loading weather. [Cached] means the network failed but an earlier successful
 * download exists, so the UI can show it together with a "last updated" note.
 */
sealed interface WeatherResult {
    data class Fresh(val weather: Weather) : WeatherResult
    data class Cached(val weather: Weather, val error: DataError) : WeatherResult
    data class Failure(val error: DataError) : WeatherResult
}
