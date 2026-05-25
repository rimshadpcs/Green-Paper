package com.rimapps.arqtest.data.repository

import com.rimapps.arqtest.domain.model.Currency
import javax.inject.Inject

class FallbackCurrencyProvider @Inject constructor() {
    fun currencies(): List<Currency> {
        return fallbackCurrencies
    }

    private companion object {
        val fallbackCurrencies = listOf(
            Currency(code = "MXN"),
            Currency(code = "ARS"),
            Currency(code = "BRL"),
            Currency(code = "COP")
        )
    }
}
