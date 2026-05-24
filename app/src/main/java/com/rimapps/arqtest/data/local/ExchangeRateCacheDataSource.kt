package com.rimapps.arqtest.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rimapps.arqtest.data.mapper.toCached
import com.rimapps.arqtest.data.mapper.toDomain
import com.rimapps.arqtest.domain.model.ExchangeRate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface ExchangeRateCacheDataSource {
    suspend fun saveRates(rates: List<ExchangeRate>)

    suspend fun getRates(): List<ExchangeRate>
}

class DataStoreExchangeRateCacheDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) : ExchangeRateCacheDataSource {
    override suspend fun saveRates(rates: List<ExchangeRate>) {
        val cachedRates = rates.map { rate -> rate.toCached() }
        val encodedRates = json.encodeToString(
            ListSerializer(CachedExchangeRate.serializer()),
            cachedRates
        )

        dataStore.edit { preferences ->
            preferences[CACHED_RATES_KEY] = encodedRates
        }
    }

    override suspend fun getRates(): List<ExchangeRate> {
        val encodedRates = dataStore.data.first()[CACHED_RATES_KEY].orEmpty()
        if (encodedRates.isBlank()) return emptyList()

        return runCatching {
            json.decodeFromString(
                ListSerializer(CachedExchangeRate.serializer()),
                encodedRates
            ).map { cachedRate -> cachedRate.toDomain() }
        }.getOrElse { emptyList() }
    }

    private companion object {
        val CACHED_RATES_KEY = stringPreferencesKey("cached_exchange_rates")
    }
}
