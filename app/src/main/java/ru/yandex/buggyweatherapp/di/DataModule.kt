package ru.yandex.buggyweatherapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.yandex.buggyweatherapp.data.impl.LocationRepositoryImpl
import ru.yandex.buggyweatherapp.data.impl.WeatherRepositoryImpl
import ru.yandex.buggyweatherapp.data.repository.LocationRepository
import ru.yandex.buggyweatherapp.data.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.LocationTracker

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideLocationRepository(
        @ApplicationContext context: Context, tracker: LocationTracker
    ): LocationRepository {
        return LocationRepositoryImpl(
            context = context,
            locationTracker = tracker
        )
    }

    @Provides
    fun provideWeatherRepository(): WeatherRepository {
        return WeatherRepositoryImpl()
    }
}