package ru.yandex.buggyweatherapp.data.impl

import android.util.Log
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.yandex.buggyweatherapp.api.RetrofitInstance
import ru.yandex.buggyweatherapp.data.repository.WeatherRepository
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import java.io.IOException

class WeatherRepositoryImpl: WeatherRepository {
    private val weatherApi = RetrofitInstance.weatherApi

    private var cachedWeatherData: WeatherData? = null

    override suspend fun getWeatherData(location: Location): Result<WeatherData> =
        withContext(Dispatchers.IO) {
            try {

                val response = weatherApi.getCurrentWeather(location.latitude, location.longitude)
                val weatherData = parseWeatherData(response)
                cachedWeatherData = weatherData

                Result.success(weatherData)
            } catch (e: HttpException) {
                Log.e("WeatherRepository", "API Error: ${e.code()}", e)
                Result.failure<WeatherData>(Exception("Unexpected Error: ${e.message}"))

            } catch (e: IOException) {
                Result.failure<WeatherData>(Exception("Network Error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure<WeatherData>(Exception("Unexpected Error: ${e.message}"))
            }
        }

    override suspend fun getWeatherByCity(cityName: String): Result<WeatherData> =
        withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getWeatherByCity(cityName)
                val weatherData = parseWeatherData(response)
                cachedWeatherData = weatherData
                Result.success(weatherData)
            } catch (e: Exception) {
                Result.failure<WeatherData>(Exception("Error fetching weather data"))
            }
        }


    override suspend fun parseWeatherData(json: JsonObject): WeatherData {

        val main = json.getAsJsonObject("main")
        val wind = json.getAsJsonObject("wind")
        val sys = json.getAsJsonObject("sys")
        val weather = json.getAsJsonArray("weather").get(0).asJsonObject
        val clouds = json.getAsJsonObject("clouds")

        return WeatherData(
            cityName = json.get("name").asString,
            country = sys.get("country").asString,
            temperature = main.get("temp").asDouble,
            feelsLike = main.get("feels_like").asDouble,
            minTemp = main.get("temp_min").asDouble,
            maxTemp = main.get("temp_max").asDouble,
            humidity = main.get("humidity").asInt,
            pressure = main.get("pressure").asInt,
            windSpeed = wind.get("speed").asDouble,
            windDirection = if (wind.has("deg")) wind.get("deg").asInt else 0,
            description = weather.get("description").asString,
            icon = weather.get("icon").asString,
            cloudiness = clouds.get("all").asInt,
            sunriseTime = sys.get("sunrise").asLong,
            sunsetTime = sys.get("sunset").asLong,
            timezone = json.get("timezone").asInt,
            timestamp = json.get("dt").asLong,
            rawApiData = json.toString(),
            rain = if (json.has("rain") && json.getAsJsonObject("rain").has("1h"))
                json.getAsJsonObject("rain").get("1h").asDouble else null,
            snow = if (json.has("snow") && json.getAsJsonObject("snow").has("1h"))
                json.getAsJsonObject("snow").get("1h").asDouble else null
        )
    }
}