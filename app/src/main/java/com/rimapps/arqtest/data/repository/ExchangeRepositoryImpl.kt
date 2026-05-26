package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.data.local.ExchangeRateCacheDataSource
import com.rimapps.arqtest.data.mapper.toCurrencyOrNull
import com.rimapps.arqtest.data.mapper.toDomain
import com.rimapps.arqtest.data.remote.ExchangeRemoteDataSource
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRatesResult
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val remoteDataSource: ExchangeRemoteDataSource,
    private val cacheDataSource: ExchangeRateCacheDataSource,
    private val fallbackCurrencyProvider: FallbackCurrencyProvider
) : ExchangeRepository {
    override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
        return try {
            val currencies = remoteDataSource.getTickerCurrencies()
                .mapNotNull { code -> code.toCurrencyOrNull() }
                .ifEmpty { fallbackCurrencyProvider.currencies() }

            AppResult.Success(currencies)
        } catch (_: Exception) {
            AppResult.Success(fallbackCurrencyProvider.currencies())
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

            val rates = remoteDataSource.getTickers(currencies).map { ticker -> ticker.toDomain() }
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
}
