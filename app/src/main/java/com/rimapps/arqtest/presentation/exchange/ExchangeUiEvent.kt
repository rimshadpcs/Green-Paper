package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.domain.model.AmountInputField

sealed interface ExchangeUiEvent {
    data class AmountChanged(
        val field: AmountInputField,
        val value: String
    ) : ExchangeUiEvent

    data class CurrencySelected(val currencyCode: String) : ExchangeUiEvent

    data object SwapClicked : ExchangeUiEvent

    data object RefreshClicked : ExchangeUiEvent

    data object RetryClicked : ExchangeUiEvent

    data object CurrencyPickerOpened : ExchangeUiEvent

    data object CurrencyPickerDismissed : ExchangeUiEvent

    data class SearchQueryChanged(val query: String) : ExchangeUiEvent
}
