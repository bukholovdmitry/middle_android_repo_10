package ru.yandex.buggyweatherapp.data.repository

import kotlinx.coroutines.flow.Flow
import ru.yandex.buggyweatherapp.model.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    suspend fun getCityNameFromLocation(location: Location): String?
    suspend fun getLocationUpdates(): Flow<Location>
}