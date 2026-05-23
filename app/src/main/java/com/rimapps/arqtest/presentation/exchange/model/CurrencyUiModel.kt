package com.rimapps.arqtest.presentation.exchange.model

import androidx.annotation.DrawableRes

data class CurrencyUiModel(
    val code: String,
    @DrawableRes val flagResId: Int
)
