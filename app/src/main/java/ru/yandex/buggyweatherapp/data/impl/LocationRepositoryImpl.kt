package ru.yandex.buggyweatherapp.data.impl

import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.yandex.buggyweatherapp.data.repository.LocationRepository
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.utils.LocationTracker
import java.util.Locale
import javax.inject.Inject

internal class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationTracker: LocationTracker
) : LocationRepository {
    companion object {
        private const val UPDATE_INTERVAL_MS = 10000L
        private const val MIN_UPDATE_INTERVAL_MS = 5000L
    }


    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var currentLocation: Location? = null

    override suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            val lastLocation =
                suspendCancellableCoroutine<android.location.Location?> { continuation ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            continuation.resume(location, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e("LocationRepository", "Error getting last location", e)
                            continuation.resume(null, null)
                        }
                }

            lastLocation?.let {
                val userLocation = Location(latitude = it.latitude, longitude = it.longitude)
                currentLocation = userLocation
                return@withContext userLocation
            }

            requestLocationUpdates()
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            null
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting location", e)
            null
        }
    }

    private suspend fun requestLocationUpdates(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
                        .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.lastLocation?.let { androidLocation ->
                            val userLocation = Location(
                                latitude = androidLocation.latitude,
                                longitude = androidLocation.longitude
                            )
                            currentLocation = userLocation
                            continuation.resume(userLocation, null)
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }

                    override fun onLocationAvailability(availability: LocationAvailability) {
                        super.onLocationAvailability(availability)
                        if (!availability.isLocationAvailable) {
                            continuation.resume(null, null)
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("LocationRepository", "Location permission not granted", e)
                continuation.resume(null, null)
            }
        }

    override suspend fun getCityNameFromLocation(location: Location): String? {
        try {

            val geocoder = Geocoder(context, Locale.getDefault())

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            return if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address.locality != null) {
                    address.locality
                } else if (address.subAdminArea != null) {
                    address.subAdminArea
                } else {
                    address.adminArea
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting city name", e)
            return null
        }
    }

    override suspend fun getLocationUpdates(): Flow<Location> =
        locationTracker.locationFlow()
            .flowOn(Dispatchers.IO)
}