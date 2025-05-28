package ru.yandex.buggyweatherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import ru.yandex.buggyweatherapp.model.Location as YandexLocation

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val isTracking = AtomicBoolean(false)

    companion object {
        private const val MIN_UPDATE_INTERVAL_MS = 5000L
        private const val MIN_UPDATE_DISTANCE_METERS = 10f

        private fun Location.toDomainLocation() = YandexLocation(
            latitude = latitude,
            longitude = longitude
        )
    }


    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<ru.yandex.buggyweatherapp.model.Location> = callbackFlow {
        if (!isTracking.compareAndSet(false, true)) {
            close(IllegalStateException("Tracking already started"))
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toDomainLocation())
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_UPDATE_INTERVAL_MS,
                MIN_UPDATE_DISTANCE_METERS,
                listener,
                Looper.getMainLooper()
            )

            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                trySend(it.toDomainLocation())
            }
        } catch (e: Exception) {
            close(e)
            isTracking.set(false)
            return@callbackFlow
        }

        awaitClose {
            locationManager.removeUpdates(listener)
            isTracking.set(false)
        }
    }.distinctUntilChanged { old, new ->
        old.latitude == new.latitude && old.longitude == new.longitude
    }

}