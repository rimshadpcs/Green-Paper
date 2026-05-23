package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.data.mapper.toDomain
import com.rimapps.arqtest.data.remote.DollarApi
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val api: DollarApi
) : ExchangeRepository {
    override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
        return try {
            val currencies = api.getTickerCurrencies()
                .mapNotNull { code -> code.toCurrencyOrNull() }
                .ifEmpty { fallbackCurrencies }

            AppResult.Success(currencies)
        } catch (exception: Exception) {
            AppResult.Success(fallbackCurrencies)
        }
    }

    override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<List<ExchangeRate>> {
        return try {
            val currencies = currencyCodes
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(separator = ",")

            if (currencies.isBlank()) {
                return AppResult.Success(emptyList())
            }

            val rates = api.getTickers(currencies).map { ticker -> ticker.toDomain() }
            AppResult.Success(rates)
        } catch (exception: Exception) {
            AppResult.Error(
                message = "Unable to load exchange rates",
                cause = exception
            )
        }
    }

    private fun String.toCurrencyOrNull(): Currency? {
        val normalizedCode = trim().uppercase()
        return normalizedCode
            .takeIf { it.isNotBlank() }
            ?.let { code -> Currency(code = code) }
    }

    private companion object {
        val fallbackCurrencies = listOf(
            Currency(code = "MXN"),
            Currency(code = "ARS"),
            Currency(code = "BRL"),
            Currency(code = "COP")
        )
    }
}
