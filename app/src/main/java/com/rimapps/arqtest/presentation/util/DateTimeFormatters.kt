package com.rimapps.arqtest.presentation.util

import java.text.SimpleDateFormat
import java.util.Locale

fun String?.toLastUpdatedDisplay(): String? {
    if (isNullOrBlank()) return null

    val normalizedDate = substringBefore(".")
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    return runCatching {
        inputFormat.parse(normalizedDate)?.let { date ->
            "Last updated: ${outputFormat.format(date)}"
        }
    }.getOrNull()
}
