package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.presentation.exchange.model.ExchangeAmountField
import com.rimapps.arqtest.domain.model.ConversionDirection
import com.rimapps.arqtest.domain.model.CurrencyAmount
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import com.rimapps.arqtest.presentation.util.maxDecimalPlacesForCurrency
import com.rimapps.arqtest.presentation.util.sanitizeAmountInput
import com.rimapps.arqtest.presentation.util.toInputAmountString
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeAmountProcessor @Inject constructor(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) {
    fun processAmountChange(
        state: ExchangeUiState,
        exchangeRates: List<ExchangeRate>,
        field: ExchangeAmountField,
        rawValue: String
    ): ExchangeUiState {
        val validationResult = sanitizeAmountInput(
            input = rawValue,
            maxDecimalPlaces = maxDecimalPlacesForCurrency(state.currencyCodeFor(field))
        )
        val sanitizedValue = validationResult.text

        if (sanitizedValue.isEmpty()) {
            return state.copy(
                topAmount = "",
                bottomAmount = "",
                topAmountError = null,
                bottomAmountError = null,
                errorMessage = null
            )
        }

        if (validationResult.isTooLarge) {
            return state.withAmount(field, sanitizedValue)
                .withClearedConvertedAmount(field)
                .withAmountError(field, AMOUNT_TOO_LARGE_MESSAGE)
                .copy(errorMessage = null)
        }

        if (sanitizedValue.isDecimalInProgress()) {
            return state.withAmount(field, sanitizedValue)
                .withClearedConvertedAmount(field)
                .withClearedAmountErrors()
                .copy(errorMessage = null)
        }

        val amount = sanitizedValue.toAmountForConversionOrNull()
            ?: return state.withAmount(field, sanitizedValue)
                .withAmountError(field, INVALID_AMOUNT_MESSAGE)

        val selectedRate = exchangeRates.rateFor(state.selectedCurrencyCode)
            ?: return state.withAmount(field, sanitizedValue).copy(
                topAmountError = null,
                bottomAmountError = null,
                errorMessage = buildMissingRateMessage(state.selectedCurrencyCode)
            )

        return convertAmount(
            state = state,
            field = field,
            value = sanitizedValue,
            amount = amount,
            exchangeRate = selectedRate
        )
    }

    private fun convertAmount(
        state: ExchangeUiState,
        field: ExchangeAmountField,
        value: String,
        amount: BigDecimal,
        exchangeRate: ExchangeRate
    ): ExchangeUiState {
        val inputCurrencyCode = state.currencyCodeFor(field)
        val direction = if (inputCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
            ConversionDirection.UsdcToQuote
        } else {
            ConversionDirection.QuoteToUsdc
        }

        return when (
            val result = convertCurrencyUseCase(
                input = CurrencyAmount(
                    currencyCode = inputCurrencyCode,
                    amount = amount
                ),
                exchangeRate = exchangeRate,
                direction = direction
            )
        ) {
            is AppResult.Success -> {
                state.withConvertedAmount(
                    editedField = field,
                    editedValue = value,
                    convertedValue = result.data.convertedAmount.toInputAmountString(
                        currencyCode = state.convertedCurrencyCodeFor(field)
                    )
                ).copy(
                    currentRate = result.data.rateUsed,
                    lastUpdated = exchangeRate.updatedAt,
                    topAmountError = null,
                    bottomAmountError = null,
                    errorMessage = null
                )
            }
            is AppResult.Error -> {
                state.withAmount(field, value)
                    .withAmountError(field, result.message)
                    .copy(errorMessage = null)
            }
        }
    }

    private fun ExchangeUiState.currencyCodeFor(field: ExchangeAmountField): String {
        return when (field) {
            ExchangeAmountField.Top -> topCurrencyCode
            ExchangeAmountField.Bottom -> bottomCurrencyCode
        }
    }

    private fun ExchangeUiState.convertedCurrencyCodeFor(field: ExchangeAmountField): String {
        return when (field) {
            ExchangeAmountField.Top -> bottomCurrencyCode
            ExchangeAmountField.Bottom -> topCurrencyCode
        }
    }

    private fun ExchangeUiState.withAmount(
        field: ExchangeAmountField,
        value: String
    ): ExchangeUiState {
        return when (field) {
            ExchangeAmountField.Top -> copy(topAmount = value)
            ExchangeAmountField.Bottom -> copy(bottomAmount = value)
        }
    }

    private fun ExchangeUiState.withConvertedAmount(
        editedField: ExchangeAmountField,
        editedValue: String,
        convertedValue: String
    ): ExchangeUiState {
        return when (editedField) {
            ExchangeAmountField.Top -> copy(
                topAmount = editedValue,
                bottomAmount = convertedValue
            )
            ExchangeAmountField.Bottom -> copy(
                topAmount = convertedValue,
                bottomAmount = editedValue
            )
        }
    }

    private fun ExchangeUiState.withClearedConvertedAmount(field: ExchangeAmountField): ExchangeUiState {
        return when (field) {
            ExchangeAmountField.Top -> copy(bottomAmount = "")
            ExchangeAmountField.Bottom -> copy(topAmount = "")
        }
    }

    private fun ExchangeUiState.withAmountError(
        field: ExchangeAmountField,
        message: String?
    ): ExchangeUiState {
        return when (field) {
            ExchangeAmountField.Top -> copy(topAmountError = message, bottomAmountError = null)
            ExchangeAmountField.Bottom -> copy(topAmountError = null, bottomAmountError = message)
        }
    }

    private fun ExchangeUiState.withClearedAmountErrors(): ExchangeUiState {
        return copy(
            topAmountError = null,
            bottomAmountError = null
        )
    }

    private fun String.isDecimalInProgress(): Boolean {
        return this == "."
    }

    private fun String.toAmountForConversionOrNull(): BigDecimal? {
        if (!matches(AMOUNT_PATTERN)) return null

        val normalizedValue = when {
            startsWith(".") -> "0$this"
            endsWith(".") -> dropLast(1)
            else -> this
        }

        if (normalizedValue.isBlank()) return null

        return runCatching { BigDecimal(normalizedValue) }
            .getOrNull()
            ?.takeIf { amount -> amount >= BigDecimal.ZERO }
    }

    private companion object {
        val AMOUNT_PATTERN = Regex("""\d+(\.\d*)?|\.\d*""")
        const val AMOUNT_TOO_LARGE_MESSAGE = "Amount is too large"
        const val INVALID_AMOUNT_MESSAGE = "Enter a valid amount"
    }
}

internal fun List<ExchangeRate>.rateFor(currencyCode: String): ExchangeRate? {
    return firstOrNull { rate ->
        rate.quoteCurrencyCode.equals(currencyCode, ignoreCase = true)
    }
}

internal fun ExchangeRate?.missingRateMessage(currencyCode: String): String? {
    return if (this == null) buildMissingRateMessage(currencyCode) else null
}

internal fun buildMissingRateMessage(currencyCode: String): String {
    return "Exchange rate unavailable for ${currencyCode.uppercase()}"
}
