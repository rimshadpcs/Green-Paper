package com.rimapps.arqtest.domain.repository

interface SelectedCurrencyRepository {
    suspend fun getSelectedCurrencyCode(): String?

    suspend fun saveSelectedCurrencyCode(currencyCode: String)

    suspend fun getSelectedQuoteCurrencyIsFirst(): Boolean?

    suspend fun saveSelectedQuoteCurrencyIsFirst(isFirst: Boolean)
}
