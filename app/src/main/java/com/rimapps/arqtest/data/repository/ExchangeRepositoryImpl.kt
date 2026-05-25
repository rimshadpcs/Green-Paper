package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.data.local.ExchangeRateCacheDataSource
import com.rimapps.arqtest.data.mapper.toDomain
import com.rimapps.arqtest.data.remote.DollarApi
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRatesResult
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val api: DollarApi,
    private val cacheDataSource: ExchangeRateCacheDataSource
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

    override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<ExchangeRatesResult> {
        return try {
            val currencies = currencyCodes
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(separator = ",")

            if (currencies.isBlank()) {
                return AppResult.Success(
                    ExchangeRatesResult(
                        rates = emptyList(),
                        isCached = false
                    )
                )
            }

            val rates = api.getTickers(currencies).map { ticker -> ticker.toDomain() }
            runCatching {
                cacheDataSource.saveRates(rates)
            }

            AppResult.Success(
                ExchangeRatesResult(
                    rates = rates,
                    isCached = false
                )
            )
        } catch (exception: Exception) {
            val cachedRates = cacheDataSource.getRates()
            if (cachedRates.isNotEmpty()) {
                return AppResult.Success(
                    ExchangeRatesResult(
                        rates = cachedRates,
                        isCached = true
                    )
                )
            }

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
