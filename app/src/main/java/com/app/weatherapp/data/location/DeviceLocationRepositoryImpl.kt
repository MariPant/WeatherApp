package com.app.weatherapp.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.repository.DeviceLocationRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Device location through the Fused Location Provider (approximate accuracy is enough). */
class DeviceLocationRepositoryImpl(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
    private val reverseGeocoder: ReverseGeocoder,
) : DeviceLocationRepository {

    override fun hasLocationPermission(): Boolean =
        isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) || isGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    override suspend fun getCurrentLocation(): DataResult<WeatherLocation> {
        if (!hasLocationPermission()) return DataResult.Failure(DataError.LocationPermissionDenied)

        val position = try {
            locate()
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            return DataResult.Failure(DataError.LocationPermissionDenied)
        } catch (e: ApiException) {
            return DataResult.Failure(DataError.LocationUnavailable)
        }
        if (position == null) return DataResult.Failure(DataError.LocationUnavailable)

        val place = reverseGeocoder.lookup(position.latitude, position.longitude)
        return DataResult.Success(
            WeatherLocation(
                id = WeatherLocation.CURRENT_LOCATION_ID,
                // Blank name means "unknown place"; the UI shows a localized "My Location" instead.
                name = place?.name.orEmpty(),
                region = place?.region,
                country = place?.country,
                countryCode = place?.countryCode,
                latitude = position.latitude,
                longitude = position.longitude,
            )
        )
    }

    /** Fresh fix with a timeout; falls back to the last known position (null if location is off). */
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission") // Checked by hasLocationPermission() before this is called.
    private suspend fun locate(): Location? {
        val cancellation = CancellationTokenSource()
        val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            fusedClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                .await(cancellation)
        }
        return fresh ?: fusedClient.lastLocation.await()
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
