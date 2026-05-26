package com.rimapps.arqtest.presentation.util

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toRateDisplay(): String {
    return setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

