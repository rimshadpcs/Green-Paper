package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.data.remote.DollarApi
import com.rimapps.arqtest.data.remote.dto.TickerDto
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeRepositoryImplTest {
    @Test
    fun `currencies API failure returns fallback currencies`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            api = FakeDollarApi(
                currenciesFailure = IllegalStateException("Not available yet")
            )
        )

        val result = repository.getAvailableCurrencies()

        val currencies = result.successData()
        assertEquals(listOf("MXN", "ARS", "BRL", "COP"), currencies.map { it.code })
    }

    @Test
    fun `tickers API success returns exchange rates`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            api = FakeDollarApi(
                tickers = listOf(
                    TickerDto(
                        ask = "18.4105000000",
                        bid = "18.4069700000",
                        book = "usdc_mxn",
                        date = "2025-10-20T20:14:57.361483956"
                    )
                )
            )
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        val rates = result.successData()
        assertEquals(1, rates.size)
        assertEquals("USDc", rates.first().baseCurrencyCode)
        assertEquals("MXN", rates.first().quoteCurrencyCode)
        assertBigDecimalEquals("18.4069700000", rates.first().bid)
        assertBigDecimalEquals("18.4105000000", rates.first().ask)
    }

    @Test
    fun `malformed ticker returns error instead of crashing`() = runBlocking {
        val repository = ExchangeRepositoryImpl(
            api = FakeDollarApi(
                tickers = listOf(
                    TickerDto(
                        ask = "18.4105000000",
                        bid = "18.4069700000",
                        book = "mxn",
                        date = "2025-10-20T20:14:57.361483956"
                    )
                )
            )
        )

        val result = repository.getExchangeRates(listOf("MXN"))

        assertTrue(result is AppResult.Error)
    }

    private class FakeDollarApi(
        private val currencies: List<String> = emptyList(),
        private val tickers: List<TickerDto> = emptyList(),
        private val currenciesFailure: Exception? = null,
        private val tickersFailure: Exception? = null
    ) : DollarApi {
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
}
