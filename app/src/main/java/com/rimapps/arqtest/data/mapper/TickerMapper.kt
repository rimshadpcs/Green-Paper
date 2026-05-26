package com.rimapps.arqtest.data.mapper

import com.rimapps.arqtest.data.remote.dto.TickerDto
import com.rimapps.arqtest.domain.model.ExchangeRate

fun TickerDto.toDomain(): ExchangeRate {
    val parts = book.split("_")
    require(parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
        "Malformed ticker book: $book"
    }

    return ExchangeRate(
        baseCurrencyCode = parts[0].toDomainCurrencyCode(),
        quoteCurrencyCode = parts[1].uppercase(),
        bid = bid.toBigDecimal(),
        ask = ask.toBigDecimal(),
        updatedAt = date
    )
}

private fun String.toDomainCurrencyCode(): String {
    return if (equals("usdc", ignoreCase = true)) {
        "USDc"
    } else {
        uppercase()
    }
}
