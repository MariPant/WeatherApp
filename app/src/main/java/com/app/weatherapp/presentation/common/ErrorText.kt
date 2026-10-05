package com.app.weatherapp.presentation.common

import androidx.annotation.StringRes
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.DataError

/** Title + explanation shown for each error, so every screen words problems the same way. */
data class ErrorText(@StringRes val title: Int, @StringRes val message: Int)

fun DataError.toErrorText(): ErrorText = when (this) {
    DataError.NoInternet -> ErrorText(R.string.error_title_weather, R.string.error_no_internet)
    DataError.Timeout -> ErrorText(R.string.error_title_weather, R.string.error_timeout)
    is DataError.Server -> ErrorText(R.string.error_title_weather, R.string.error_server)
    DataError.CityNotFound -> ErrorText(R.string.error_title_city, R.string.error_city_not_found)
    DataError.LocationPermissionDenied -> ErrorText(R.string.error_title_location, R.string.error_location_permission)
    DataError.LocationUnavailable -> ErrorText(R.string.error_title_location, R.string.error_location_unavailable)
    DataError.Unknown -> ErrorText(R.string.error_title_weather, R.string.error_unknown)
}

/** Short one-line reason, used in the offline banner and snackbars. */
@StringRes
fun DataError.shortMessage(): Int = when (this) {
    DataError.NoInternet -> R.string.offline_no_internet
    DataError.Timeout -> R.string.offline_timeout
    is DataError.Server, DataError.Unknown -> R.string.offline_server
    DataError.CityNotFound -> R.string.error_city_not_found
    DataError.LocationPermissionDenied -> R.string.error_location_permission_short
    DataError.LocationUnavailable -> R.string.error_location_unavailable_short
}
