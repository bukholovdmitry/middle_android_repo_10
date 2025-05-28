package ru.yandex.buggyweatherapp.model

sealed interface ScreenState {
    data object Default : ScreenState
    data object Loading : ScreenState
    data class Success(val weatherData: WeatherData) : ScreenState
    data class Error(val message: String) : ScreenState
}