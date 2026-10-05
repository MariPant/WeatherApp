package com.app.weatherapp.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

/** Place name for coordinates via the platform Geocoder (as in the original WeatherWizard code). */
class ReverseGeocoder(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) {
    data class Place(val name: String, val region: String?, val country: String?, val countryCode: String?)

    suspend fun lookup(latitude: Double, longitude: Double): Place? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        continuation.resume(addresses.firstOrNull())
                    }

                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                })
            }
        } else {
            withContext(ioDispatcher) { legacyLookup(geocoder, latitude, longitude) }
        }
        return address?.toPlace()
    }

    @Suppress("DEPRECATION") // The listener-based API only exists on API 33+.
    private fun legacyLookup(geocoder: Geocoder, latitude: Double, longitude: Double): Address? = try {
        geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
    } catch (e: IOException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun Address.toPlace(): Place? {
        val name = locality ?: subAdminArea ?: adminArea ?: return null
        return Place(name = name, region = adminArea, country = countryName, countryCode = countryCode)
    }
}
