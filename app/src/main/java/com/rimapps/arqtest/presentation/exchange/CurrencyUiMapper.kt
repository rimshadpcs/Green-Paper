package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.R
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.presentation.exchange.model.CurrencyUiModel

fun Currency.toUiModel(): CurrencyUiModel {
    return CurrencyUiModel(
        code = code,
        flagResId = code.flagDrawableResId()
    )
}

fun String.flagDrawableResId(): Int {
    return when (uppercase()) {
        "USDC", "USD" -> R.drawable.ic_usa_flag
        "MXN" -> R.drawable.ic_mexico_flag
        "ARS" -> R.drawable.ic_argentina_flag
        "BRL" -> R.drawable.ic_brazil_flag
        "COP" -> R.drawable.ic_colombia_flag
        else -> R.drawable.ic_usa_flag
    }
}
