package com.rimapps.arqtest.domain.usecase

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.domain.model.ConversionDirection
import com.rimapps.arqtest.domain.model.CurrencyAmount
import com.rimapps.arqtest.domain.model.ExchangeRate
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertCurrencyUseCaseTest {
    private val useCase = ConvertCurrencyUseCase()

    @Test
    fun `USDc to MXN conversion uses midpoint rate`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "USDc", amount = BigDecimal("10")),
            exchangeRate = mxnRate(bid = "17.00", ask = "19.00"),
            direction = ConversionDirection.UsdcToQuote
        )

        val conversion = result.successData()
        assertBigDecimalEquals("180.00", conversion.convertedAmount)
        assertBigDecimalEquals("18.00", conversion.rateUsed)
        assertEquals(ConversionDirection.UsdcToQuote, conversion.direction)
    }

    @Test
    fun `MXN to USDc conversion uses midpoint rate`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "MXN", amount = BigDecimal("180")),
            exchangeRate = mxnRate(bid = "17.00", ask = "19.00"),
            direction = ConversionDirection.QuoteToUsdc
        )

        val conversion = result.successData()
        assertBigDecimalEquals("10", conversion.convertedAmount)
        assertBigDecimalEquals("18.00", conversion.rateUsed)
        assertEquals(ConversionDirection.QuoteToUsdc, conversion.direction)
    }

    @Test
    fun `midpoint is calculated from bid and ask`() {
        val rate = mxnRate(bid = "16.75", ask = "17.25")

        assertBigDecimalEquals("17.00", rate.midpoint)
    }

    @Test
    fun `zero rate returns error`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "USDc", amount = BigDecimal("10")),
            exchangeRate = mxnRate(bid = "0", ask = "0"),
            direction = ConversionDirection.UsdcToQuote
        )

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `negative amount returns error`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "USDc", amount = BigDecimal("-1")),
            exchangeRate = mxnRate(bid = "17.00", ask = "19.00"),
            direction = ConversionDirection.UsdcToQuote
        )

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `large amount does not crash`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "USDc", amount = BigDecimal("999999999999999999.99")),
            exchangeRate = mxnRate(bid = "17.00", ask = "19.00"),
            direction = ConversionDirection.UsdcToQuote
        )

        val conversion = result.successData()
        assertBigDecimalEquals("17999999999999999999.8200", conversion.convertedAmount)
    }

    @Test
    fun `decimal amount converts correctly`() {
        val result = useCase(
            input = CurrencyAmount(currencyCode = "USDc", amount = BigDecimal("12.34")),
            exchangeRate = mxnRate(bid = "17.10", ask = "17.30"),
            direction = ConversionDirection.UsdcToQuote
        )

        val conversion = result.successData()
        assertBigDecimalEquals("212.2480", conversion.convertedAmount)
        assertBigDecimalEquals("17.20", conversion.rateUsed)
    }

    @Test
    fun `quote currency miss match returns error  `(){
        val result = useCase(
            input = CurrencyAmount(currencyCode = "ARS", amount = BigDecimal("180")),
            exchangeRate = mxnRate(bid = "17.00", ask = "19.00"),
            direction = ConversionDirection.QuoteToUsdc
        )
        assertTrue(result is AppResult.Error)
    }

    private fun mxnRate(
        bid: String,
        ask: String
    ) = ExchangeRate(
        baseCurrencyCode = "USDc",
        quoteCurrencyCode = "MXN",
        bid = BigDecimal(bid),
        ask = BigDecimal(ask),
        updatedAt = "2026-05-22T00:00:00Z"
    )

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
