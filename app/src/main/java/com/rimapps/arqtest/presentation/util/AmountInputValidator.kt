package com.rimapps.arqtest.presentation.util

import java.math.BigDecimal
import java.math.RoundingMode

data class AmountInputValidationResult(
    val text: String,
    val selection: Int,
    val isTooLarge: Boolean = false
)

fun sanitizeAmountInput(
    input: String,
    selectionEnd: Int = input.length,
    maxDecimalPlaces: Int,
    maxIntegerDigits: Int = MAX_INTEGER_DIGITS,
    maxStoredIntegerDigits: Int = MAX_STORED_INTEGER_DIGITS
): AmountInputValidationResult {
    var hasDecimalSeparator = false
    var integerDigits = 0
    var decimalDigits = 0
    var sanitizedSelection = 0
    var isTooLarge = false

    val sanitizedText = buildString {
        input.forEachIndexed { index, char ->
            val shouldCountForSelection = index < selectionEnd
            val appended = when {
                char.isDigit() && !hasDecimalSeparator && integerDigits < maxStoredIntegerDigits -> {
                    integerDigits += 1
                    if (integerDigits > maxIntegerDigits) {
                        isTooLarge = true
                    }
                    append(char)
                    true
                }
                char.isDigit() && hasDecimalSeparator && decimalDigits < maxDecimalPlaces -> {
                    decimalDigits += 1
                    append(char)
                    true
                }
                char == '.' && !hasDecimalSeparator -> {
                    hasDecimalSeparator = true
                    append(char)
                    true
                }
                else -> false
            }

            if (appended && shouldCountForSelection) {
                sanitizedSelection += 1
            }
        }
    }

    return AmountInputValidationResult(
        text = sanitizedText,
        selection = sanitizedSelection.coerceIn(0, sanitizedText.length),
        isTooLarge = isTooLarge
    )
}

fun maxDecimalPlacesForCurrency(currencyCode: String): Int {
    return if (currencyCode.equals("USDc", ignoreCase = true)) {
        USDC_DECIMAL_PLACES
    } else {
        FIAT_DECIMAL_PLACES
    }
}

fun BigDecimal.toInputAmountString(currencyCode: String): String {
    return if (currencyCode.equals("USDc", ignoreCase = true)) {
        setScale(USDC_DECIMAL_PLACES, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
            .normalizeZeroScale()
    } else {
        setScale(FIAT_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()
    }
}

private fun String.normalizeZeroScale(): String {
    return if (contains("E")) {
        BigDecimal(this).toPlainString()
    } else {
        this
    }
}

private const val USDC_DECIMAL_PLACES = 6
private const val FIAT_DECIMAL_PLACES = 2
private const val MAX_INTEGER_DIGITS = 12
private const val MAX_STORED_INTEGER_DIGITS = 24
