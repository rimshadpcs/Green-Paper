package com.rimapps.arqtest.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rimapps.arqtest.domain.repository.SelectedCurrencyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class DataStoreSelectedCurrencyRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SelectedCurrencyRepository {
    override suspend fun getSelectedCurrencyCode(): String? {
        return dataStore.data.first()[SELECTED_QUOTE_CURRENCY_KEY]
            ?.trim()
            ?.uppercase()
            ?.takeIf { currencyCode -> currencyCode.isNotBlank() }
    }

    override suspend fun saveSelectedCurrencyCode(currencyCode: String) {
        val normalizedCurrencyCode = currencyCode.trim().uppercase()
        if (normalizedCurrencyCode.isBlank()) return

        dataStore.edit { preferences ->
            preferences[SELECTED_QUOTE_CURRENCY_KEY] = normalizedCurrencyCode
        }
    }

    override suspend fun getSelectedQuoteCurrencyIsFirst(): Boolean? {
        return dataStore.data.first()[SELECTED_QUOTE_CURRENCY_IS_FIRST_KEY]
    }

    override suspend fun saveSelectedQuoteCurrencyIsFirst(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[SELECTED_QUOTE_CURRENCY_IS_FIRST_KEY] = isFirst
        }
    }

    private companion object {
        val SELECTED_QUOTE_CURRENCY_KEY = stringPreferencesKey("selected_quote_currency")
        val SELECTED_QUOTE_CURRENCY_IS_FIRST_KEY = booleanPreferencesKey("selected_quote_currency_is_first")
    }
}
