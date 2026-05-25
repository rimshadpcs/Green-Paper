package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeAmountProcessorTest {
    private val processor = ExchangeAmountProcessor(ConvertCurrencyUseCase())

    @Test
    fun `empty input clears both fields`() {
        val state = state(
            topAmount = "10",
            bottomAmount = "180.00",
            topAmountError = "error",
            errorMessage = "error"
        )

        val result = processor.processAmountChange(
            state = state,
            field = AmountInputField.Top,
            rawValue = ""
        )

        assertEquals("", result.topAmount)
        assertEquals("", result.bottomAmount)
        assertNull(result.topAmountError)
        assertNull(result.bottomAmountError)
        assertNull(result.errorMessage)
    }

    @Test
    fun `decimal point input is allowed as in progress decimal`() {
        val result = processor.processAmountChange(
            state = state(),
            field = AmountInputField.Top,
            rawValue = "."
        )

        assertEquals(".", result.topAmount)
        assertEquals("", result.bottomAmount)
        assertNull(result.topAmountError)
        assertNull(result.bottomAmountError)
        assertNull(result.errorMessage)
    }

    @Test
    fun `too large input sets amount error and clears converted amount`() {
        val result = processor.processAmountChange(
            state = state(bottomAmount = "180.00"),
            field = AmountInputField.Top,
            rawValue = "9999999999999999999"
        )

        assertEquals("9999999999999999999", result.topAmount)
        assertEquals("", result.bottomAmount)
        assertEquals("Amount is too large", result.topAmountError)
        assertNull(result.bottomAmountError)
        assertNull(result.errorMessage)
    }

    @Test
    fun `missing rate sets rate unavailable state`() {
        val result = processor.processAmountChange(
            state = state(
                selectedCurrencyCode = "COP",
                exchangeRates = listOf(mxnRate())
            ),
            field = AmountInputField.Top,
            rawValue = "10"
        )

        assertEquals("10", result.topAmount)
        assertEquals("", result.bottomAmount)
        assertNull(result.topAmountError)
        assertNull(result.bottomAmountError)
        assertTrue(result.errorMessage.orEmpty().contains("COP"))
    }

    @Test
    fun `valid USDc input converts quote amount`() {
        val result = processor.processAmountChange(
            state = state(),
            field = AmountInputField.Top,
            rawValue = "10"
        )

        assertEquals("10", result.topAmount)
        assertEquals("180.00", result.bottomAmount)
        assertBigDecimalEquals("18.00", result.currentRate)
        assertEquals("2026-05-23T00:00:00Z", result.lastUpdated)
        assertNull(result.topAmountError)
        assertNull(result.bottomAmountError)
        assertNull(result.errorMessage)
    }

    @Test
    fun `valid quote input converts USDc amount`() {
        val result = processor.processAmountChange(
            state = state(),
            field = AmountInputField.Bottom,
            rawValue = "180"
        )

        assertEquals("10", result.topAmount)
        assertEquals("180", result.bottomAmount)
        assertBigDecimalEquals("18.00", result.currentRate)
        assertEquals("2026-05-23T00:00:00Z", result.lastUpdated)
        assertNull(result.topAmountError)
        assertNull(result.bottomAmountError)
        assertNull(result.errorMessage)
    }

    private fun state(
        selectedCurrencyCode: String = "MXN",
        exchangeRates: List<ExchangeRate> = listOf(mxnRate()),
        topAmount: String = "",
        bottomAmount: String = "",
        topAmountError: String? = null,
        bottomAmountError: String? = null,
        errorMessage: String? = null
    ): ExchangeUiState {
        return ExchangeUiState(
            selectedCurrencyCode = selectedCurrencyCode,
            exchangeRates = exchangeRates,
            topCurrencyCode = ExchangeUiState.BASE_CURRENCY,
            bottomCurrencyCode = selectedCurrencyCode,
            topAmount = topAmount,
            bottomAmount = bottomAmount,
            topAmountError = topAmountError,
            bottomAmountError = bottomAmountError,
            errorMessage = errorMessage
        )
    }

    private fun mxnRate() = ExchangeRate(
        baseCurrencyCode = "USDc",
        quoteCurrencyCode = "MXN",
        bid = BigDecimal("17.00"),
        ask = BigDecimal("19.00"),
        updatedAt = "2026-05-23T00:00:00Z"
    )

    private fun assertBigDecimalEquals(
        expected: String,
        actual: BigDecimal?
    ) {
        assertEquals(BigDecimal(expected).stripTrailingZeros(), actual?.stripTrailingZeros())
    }
}
