package com.rimapps.arqtest.domain.model

import java.math.BigDecimal

data class ConversionResult(
    val convertedAmount: BigDecimal,
    val rateUsed: BigDecimal,
    val direction: ConversionDirection
)
