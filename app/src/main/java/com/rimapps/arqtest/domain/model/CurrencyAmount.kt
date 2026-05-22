package com.rimapps.arqtest.domain.model

import java.math.BigDecimal

data class CurrencyAmount(
    val currencyCode: String,
    val amount: BigDecimal
)
