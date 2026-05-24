package com.rimapps.arqtest.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CachedExchangeRate(
    val baseCurrencyCode: String,
    val quoteCurrencyCode: String,
    val bid: String,
    val ask: String,
    val updatedAt: String
)
