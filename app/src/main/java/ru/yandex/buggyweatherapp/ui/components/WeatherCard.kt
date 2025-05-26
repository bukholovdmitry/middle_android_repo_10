package ru.yandex.buggyweatherapp.ui.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import ru.yandex.buggyweatherapp.R
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.utils.ImageLoader
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper

@Composable
fun DetailedWeatherCard(
    weather: WeatherData, onFavoriteClick: () -> Unit, onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            HeaderSection(weather, onFavoriteClick, onRefreshClick)
            TemperatureSection(weather)
            WeatherDataDetails(weather)
        }
    }
}

@Composable
private fun HeaderSection(
    weather: WeatherData,
    onFavoriteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weather.cityName, style = MaterialTheme.typography.headlineMedium
        )

        Row {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            }

            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh, contentDescription = "Refresh"
                )
            }
        }
    }
}

@Composable
private fun ActionsRow(
    onFavoriteClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isFavorite: Boolean
) {
    Row {
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite_icon_desc),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.refresh_icon_desc)
            )
        }
    }
}

@Composable
private fun TemperatureSection(weather: WeatherData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {

        AsyncImage(
            model = "https://openweathermap.org/img/wn/${weather.icon}@2x.png",
            contentDescription = stringResource(R.string.weather_icon_desc),
            modifier = Modifier.size(50.dp)
        )

        Text(
            text = weather.temperature.toString() + "°C",
            style = MaterialTheme.typography.headlineLarge
        )
    }

    Text(
        text = weather.description.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun WeatherDataDetails(weather: WeatherData){
    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn {
        item {
            WeatherDataRow(
                label = stringResource(R.string.feels_like),
                value = stringResource(R.string.temperature_format, weather.feelsLike)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.min_max),
                value = stringResource(R.string.min_max_format, weather.minTemp, weather.maxTemp)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.humidity),
                value = stringResource(R.string.percent_format, weather.humidity)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.pressure),
                value = stringResource(R.string.pressure_format, weather.pressure)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.wind),
                value = stringResource(R.string.wind_format, weather.windSpeed)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.sunrise),
                value = WeatherIconMapper.formatTimestamp(weather.sunriseTime)
            )
        }
        item {
            WeatherDataRow(
                label = stringResource(R.string.sunset),
                value = WeatherIconMapper.formatTimestamp(weather.sunsetTime)
            )
        }
    }
}

@Composable
private fun WeatherDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}