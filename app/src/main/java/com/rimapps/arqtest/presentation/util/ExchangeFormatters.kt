package com.rimapps.arqtest.presentation.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val amountFormat = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US))

fun BigDecimal.toRateDisplay(): String {
    return setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

fun String.toGroupedAmountOrSelf(): String {
    val amount = runCatching { BigDecimal(this) }.getOrNull() ?: return this
    return amountFormat.format(amount)
}
