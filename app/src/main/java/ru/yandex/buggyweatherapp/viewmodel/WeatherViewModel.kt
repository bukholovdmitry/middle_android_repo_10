package ru.yandex.buggyweatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.data.repository.LocationRepository
import ru.yandex.buggyweatherapp.data.repository.WeatherRepository
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.ScreenState
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScreenState>(ScreenState.Default)
    val uiState: StateFlow<ScreenState> = _uiState.asStateFlow()

    private val _currentLocation = MutableLiveData<Location>()
    private var locationUpdatesJob: Job? = null

    private var refreshJob: Job? = null

    companion object {
        const val AUTO_REFRESH_DELAY = 10000L
    }

    fun initialize() {
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun fetchCurrentLocationWeather() {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()

            if (location != null) {
                _currentLocation.value = location
                getWeatherForLocation(location)
            } else {
                _uiState.value = ScreenState.Error("Unable to get current location")
            }
        }
    }

    private fun getWeatherForLocation(location: Location) {
        val state = _uiState.value
        viewModelScope.launch {
            val data = weatherRepository.getWeatherData(location)

            if (data.isSuccess) {
                val weatherData = data.getOrThrow()
                if (state is ScreenState.Success) {
                    weatherData.isFavorite = state.weatherData.isFavorite
                }
                _uiState.value = ScreenState.Success(weatherData)
            } else {
                _uiState.value =
                    ScreenState.Error(data.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            _uiState.value = ScreenState.Error("City name cannot be empty")
            return
        }
        _uiState.value = ScreenState.Loading

        viewModelScope.launch {

            val data = weatherRepository.getWeatherByCity(city)

            if (data.isSuccess) {
                _uiState.value = ScreenState.Success(data.getOrThrow())
            } else {
                _uiState.value =
                    ScreenState.Error(data.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_DELAY)

                _currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is ScreenState.Success) {
            val updatedWeather = currentState.weatherData.copy(
                isFavorite = !currentState.weatherData.isFavorite
            )
            _uiState.value = ScreenState.Success(updatedWeather)
        }
    }

    fun requestLocation() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            locationRepository.getLocationUpdates()
                .catch { e ->
                    Log.e("WeatherViewModel", "Location updates error", e)
                    _uiState.value = ScreenState.Error("Location tracking failed")
                }
                .collect { location ->
                    _currentLocation.value = location
                    getWeatherForLocation(location)
                }
        }

    }
}