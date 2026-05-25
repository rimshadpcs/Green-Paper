package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.presentation.exchange.model.CurrencyUiModel
import java.math.BigDecimal

data class ExchangeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val availableCurrencies: List<CurrencyUiModel> = emptyList(),
    val selectedCurrencyCode: String = DEFAULT_QUOTE_CURRENCY,
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val topCurrencyCode: String = BASE_CURRENCY,
    val bottomCurrencyCode: String = DEFAULT_QUOTE_CURRENCY,
    val topAmount: String = "",
    val bottomAmount: String = "",
    val activeAmountField: AmountInputField = AmountInputField.Top,
    val topAmountError: String? = null,
    val bottomAmountError: String? = null,
    val currentRate: BigDecimal? = null,
    val lastUpdated: String? = null,
    val isUsingCachedRates: Boolean = false,
    val isCurrencyPickerVisible: Boolean = false,
    val currencySearchQuery: String = ""
) {
    companion object {
        const val BASE_CURRENCY = "USDc"
        const val DEFAULT_QUOTE_CURRENCY = "MXN"
    }
}
