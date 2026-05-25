package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.data.local.ExchangeRateCacheDataSource
import com.rimapps.arqtest.data.remote.ExchangeRemoteDataSource
import com.rimapps.arqtest.data.remote.dto.TickerDto
import com.rimapps.arqtest.domain.model.ExchangeRate
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeRepositoryImplTest {
    @Test
    fun `currencies API failure returns fallback currencies`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                currenciesFailure = IllegalStateException("Not available yet")
            ),
            cacheDataSource = cacheDataSource(),
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        val result = repository.getAvailableCurrencies()

        val currencies = result.successData()
        assertEquals(listOf("MXN", "ARS", "BRL", "COP"), currencies.map { it.code })
    }

    @Test
    fun `tickers API success returns exchange rates`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                tickers = listOf(
                    TickerDto(
                        ask = "18.4105000000",
                        bid = "18.4069700000",
                        book = "usdc_mxn",
                        date = "2025-10-20T20:14:57.361483956"
                    )
                )
            ),
            cacheDataSource = cacheDataSource(),
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        val rates = result.successData().rates
        assertEquals(1, rates.size)
        assertEquals("USDc", rates.first().baseCurrencyCode)
        assertEquals("MXN", rates.first().quoteCurrencyCode)
        assertBigDecimalEquals("18.4069700000", rates.first().bid)
        assertBigDecimalEquals("18.4105000000", rates.first().ask)
    }

    @Test
    fun `malformed ticker returns error instead of crashing`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                tickers = listOf(
                    TickerDto(
                        ask = "18.4105000000",
                        bid = "18.4069700000",
                        book = "mxn",
                        date = "2025-10-20T20:14:57.361483956"
                    )
                )
            ),
            cacheDataSource = cacheDataSource(),
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `API success saves rates to cache`() = runBlocking {
        val cacheDataSource = cacheDataSource()
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                tickers = listOf(
                    TickerDto(
                        ask = "18.4105000000",
                        bid = "18.4069700000",
                        book = "usdc_mxn",
                        date = "2025-10-20T20:14:57.361483956"
                    )
                )
            ),
            cacheDataSource = cacheDataSource,
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        repository.getExchangeRates(listOf("MXN"))

        val cachedRates = cacheDataSource.getRates()
        assertEquals(1, cachedRates.size)
        assertEquals("MXN", cachedRates.first().quoteCurrencyCode)
        assertBigDecimalEquals("18.4069700000", cachedRates.first().bid)
    }

    @Test
    fun `API failure returns cached rates when cache exists`() = runBlocking {
        val cacheDataSource = cacheDataSource()
        cacheDataSource.saveRates(listOf(mxnRate()))
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                tickersFailure = IllegalStateException("No connection")
            ),
            cacheDataSource = cacheDataSource,
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        val ratesResult = result.successData()
        assertTrue(ratesResult.isCached)
        assertEquals(1, ratesResult.rates.size)
        assertEquals("MXN", ratesResult.rates.first().quoteCurrencyCode)
    }

    @Test
    fun `API failure with empty cache returns error`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            remoteDataSource = FakeExchangeRemoteDataSource(
                tickersFailure = IllegalStateException("No connection")
            ),
            cacheDataSource = cacheDataSource(),
            fallbackCurrencyProvider = FallbackCurrencyProvider()
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        assertTrue(result is AppResult.Error)
    }

    private class FakeExchangeRemoteDataSource(
        private val currencies: List<String> = emptyList(),
        private val tickers: List<TickerDto> = emptyList(),
        private val currenciesFailure: Exception? = null,
        private val tickersFailure: Exception? = null
    ) : ExchangeRemoteDataSource {
        override suspend fun getTickers(currencies: String): List<TickerDto> {
            tickersFailure?.let { throw it }
            return tickers
        }

        override suspend fun getTickerCurrencies(): List<String> {
            currenciesFailure?.let { throw it }
            return currencies
        }
    }

    private fun <T> AppResult<T>.successData(): T {
        assertTrue(this is AppResult.Success)
        return (this as AppResult.Success).data
    }

    private fun assertBigDecimalEquals(
        expected: String,
        actual: BigDecimal
    ) {
        assertEquals(BigDecimal(expected).stripTrailingZeros(), actual.stripTrailingZeros())
    }

    private fun cacheDataSource(): FakeExchangeRateCacheDataSource = FakeExchangeRateCacheDataSource()

    private fun mxnRate() = ExchangeRate(
        baseCurrencyCode = "USDc",
        quoteCurrencyCode = "MXN",
        bid = BigDecimal("18.4069700000"),
        ask = BigDecimal("18.4105000000"),
        updatedAt = "2025-10-20T20:14:57.361483956"
    )

    private class FakeExchangeRateCacheDataSource(
        private var rates: List<ExchangeRate> = emptyList()
    ) : ExchangeRateCacheDataSource {
        override suspend fun saveRates(rates: List<ExchangeRate>) {
            this.rates = rates
        }

        override suspend fun getRates(): List<ExchangeRate> {
            return rates
        }
    }
}
