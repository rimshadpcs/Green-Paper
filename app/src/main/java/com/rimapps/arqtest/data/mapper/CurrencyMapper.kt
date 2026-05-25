package com.rimapps.arqtest.data.mapper

import com.rimapps.arqtest.domain.model.Currency

fun String.toCurrencyOrNull(): Currency? {
    val normalizedCode = trim().uppercase()
    return normalizedCode
        .takeIf { code -> code.isNotBlank() }
        ?.let { code -> Currency(code = code) }
}
