package com.rimapps.arqtest.domain.repository

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate

interface ExchangeRepository {
    suspend fun getAvailableCurrencies(): AppResult<List<Currency>>

    suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<List<ExchangeRate>>
}
