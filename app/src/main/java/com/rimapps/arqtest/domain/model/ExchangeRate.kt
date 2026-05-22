package com.rimapps.arqtest.domain.model

import java.math.BigDecimal
import java.math.MathContext

data class ExchangeRate(
    val baseCurrencyCode: String,
    val quoteCurrencyCode: String,
    val bid: BigDecimal,
    val ask: BigDecimal,
    val updatedAt: String
) {
    val midpoint: BigDecimal
        get() = bid.add(ask).divide(TWO, MathContext.DECIMAL128)

    private companion object {
        val TWO = BigDecimal("2")
    }
}
