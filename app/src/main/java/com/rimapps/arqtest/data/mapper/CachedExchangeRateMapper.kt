package com.rimapps.arqtest.data.mapper

import com.rimapps.arqtest.data.local.CachedExchangeRate
import com.rimapps.arqtest.domain.model.ExchangeRate

fun ExchangeRate.toCached(): CachedExchangeRate {
    return CachedExchangeRate(
        baseCurrencyCode = baseCurrencyCode,
        quoteCurrencyCode = quoteCurrencyCode,
        bid = bid.toPlainString(),
        ask = ask.toPlainString(),
        updatedAt = updatedAt
    )
}

fun CachedExchangeRate.toDomain(): ExchangeRate {
    return ExchangeRate(
        baseCurrencyCode = baseCurrencyCode,
        quoteCurrencyCode = quoteCurrencyCode,
        bid = bid.toBigDecimal(),
        ask = ask.toBigDecimal(),
        updatedAt = updatedAt
    )
}
