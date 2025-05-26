package ru.yandex.buggyweatherapp.data.repository

import ru.yandex.buggyweatherapp.model.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    suspend fun getCityNameFromLocation(location: Location): String?
    suspend fun startLocationTracking()
}