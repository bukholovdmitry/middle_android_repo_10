package ru.yandex.buggyweatherapp.data.repository

import com.google.gson.JsonObject
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData

interface WeatherRepository {

    suspend fun getWeatherData(location: Location): Result<WeatherData>

    suspend fun getWeatherByCity(cityName: String): Result<WeatherData>

    suspend fun parseWeatherData(json: JsonObject): WeatherData
}