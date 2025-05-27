package ru.yandex.buggyweatherapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.yandex.buggyweatherapp.model.ScreenState
import ru.yandex.buggyweatherapp.ui.components.DetailedWeatherCard
import ru.yandex.buggyweatherapp.ui.components.LocationSearch
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()

) {

    DisposableEffect(Unit) {
        viewModel.initialize()
        onDispose {}
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationSearch(
            onCitySearch = { searchText -> viewModel.searchWeatherByCity(searchText) },
            onLocationRequest = { viewModel.requestLocation() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is ScreenState.Default -> {
            }

            is ScreenState.Loading -> {
                Text("Loading weather data...")
            }

            is ScreenState.Success -> {
                DetailedWeatherCard(weather = state.weatherData,
                    onFavoriteClick = { viewModel.toggleFavorite() },
                    onRefreshClick = { viewModel.fetchCurrentLocationWeather() })
            }

            is ScreenState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}