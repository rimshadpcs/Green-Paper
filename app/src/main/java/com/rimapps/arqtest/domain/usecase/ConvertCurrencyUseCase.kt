package com.rimapps.arqtest.domain.usecase

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.domain.model.ConversionDirection
import com.rimapps.arqtest.domain.model.ConversionResult
import com.rimapps.arqtest.domain.model.CurrencyAmount
import com.rimapps.arqtest.domain.model.ExchangeRate
import java.math.BigDecimal
import java.math.MathContext

class ConvertCurrencyUseCase {
    operator fun invoke(
        input: CurrencyAmount,
        exchangeRate: ExchangeRate,
        direction: ConversionDirection
    ): AppResult<ConversionResult> {
        if (input.amount < BigDecimal.ZERO) {
            return AppResult.Error("Amount must be zero or greater")
        }



        val midpointRate = exchangeRate.midpoint
        if (midpointRate <= BigDecimal.ZERO) {
            return AppResult.Error("Exchange rate must be greater than zero")
        }

        if (!isCurrencyValidForDirection(input.currencyCode, exchangeRate, direction)) {
            return AppResult.Error("Input currency does not match conversion direction")
        }

        val convertedAmount = when (direction) {
            ConversionDirection.UsdcToQuote -> input.amount.multiply(midpointRate)
            ConversionDirection.QuoteToUsdc -> input.amount.divide(midpointRate, MathContext.DECIMAL128)
        }

        return AppResult.Success(
            ConversionResult(
                convertedAmount = convertedAmount,
                rateUsed = midpointRate,
                direction = direction
            )
        )
    }
    private fun isCurrencyValidForDirection(
        inputCurrencyCode: String,
        exchangeRate: ExchangeRate,
        direction: ConversionDirection
    ): Boolean {
        return when (direction) {
            ConversionDirection.UsdcToQuote ->
                inputCurrencyCode.equals(exchangeRate.baseCurrencyCode, ignoreCase = true)

            ConversionDirection.QuoteToUsdc ->
                inputCurrencyCode.equals(exchangeRate.quoteCurrencyCode, ignoreCase = true)
        }
    }
}
