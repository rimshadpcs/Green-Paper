package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate
import java.math.BigDecimal

data class ExchangeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val availableCurrencies: List<Currency> = emptyList(),
    val selectedCurrencyCode: String = DEFAULT_QUOTE_CURRENCY,
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val topCurrencyCode: String = BASE_CURRENCY,
    val bottomCurrencyCode: String = DEFAULT_QUOTE_CURRENCY,
    val topAmount: String = "",
    val bottomAmount: String = "",
    val topAmountError: String? = null,
    val bottomAmountError: String? = null,
    val currentRate: BigDecimal? = null,
    val lastUpdated: String? = null,
    val isCurrencyPickerVisible: Boolean = false,
    val currencySearchQuery: String = ""
) {
    companion object {
        const val BASE_CURRENCY = "USDc"
        const val DEFAULT_QUOTE_CURRENCY = "MXN"
    }
}
