package com.rimapps.arqtest.domain.model

data class ExchangeRatesResult(
    val rates: List<ExchangeRate>,
    val isCached: Boolean
)
