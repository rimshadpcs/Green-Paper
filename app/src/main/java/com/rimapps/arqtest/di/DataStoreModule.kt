package com.rimapps.arqtest.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.rimapps.arqtest.data.local.DataStoreExchangeRateCacheDataSource
import com.rimapps.arqtest.data.local.ExchangeRateCacheDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(DATA_STORE_NAME)
        }
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    @Provides
    @Singleton
    fun provideExchangeRateCacheDataSource(
        dataStore: DataStore<Preferences>,
        json: Json
    ): ExchangeRateCacheDataSource {
        return DataStoreExchangeRateCacheDataSource(
            dataStore = dataStore,
            json = json
        )
    }

    private const val DATA_STORE_NAME = "exchange_preferences"
}
