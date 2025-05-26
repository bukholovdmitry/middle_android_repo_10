package ru.yandex.buggyweatherapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.yandex.buggyweatherapp.data.repository.LocationRepository
import ru.yandex.buggyweatherapp.data.impl.LocationRepositoryImpl
import ru.yandex.buggyweatherapp.data.impl.WeatherRepositoryImpl
import ru.yandex.buggyweatherapp.data.repository.WeatherRepository

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository {
        return LocationRepositoryImpl(
            context = context
        )
    }

    @Provides
    fun provideWeatherRepository(): WeatherRepository{
        return WeatherRepositoryImpl()
    }
}