package com.rimapps.arqtest.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isBlank()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val decimalIndex = original.indexOf('.')
        val integerPart = if (decimalIndex >= 0) {
            original.substring(startIndex = 0, endIndex = decimalIndex)
        } else {
            original
        }
        val decimalPart = if (decimalIndex >= 0) {
            original.substring(startIndex = decimalIndex)
        } else {
            ""
        }
        val groupedInteger = integerPart.reversed()
            .chunked(size = 3)
            .joinToString(separator = ",")
            .reversed()
            .ifBlank { "0" }
        val transformed = "$$groupedInteger$decimalPart"

        return TransformedText(
            text = AnnotatedString(transformed),
            offsetMapping = AmountOffsetMapping(
                original = original,
                transformed = transformed
            )
        )
    }
}

private class AmountOffsetMapping(
    private val original: String,
    private val transformed: String
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 1
        val targetRawChars = original.take(offset).count { char -> char.isDigit() || char == '.' }
        var seenRawChars = 0
        transformed.forEachIndexed { index, char ->
            if (char.isDigit() || char == '.') {
                seenRawChars += 1
            }
            if (seenRawChars == targetRawChars) {
                return index + 1
            }
        }
        return transformed.length
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset <= 1) return 0
        val targetRawChars = transformed.take(offset).count { char -> char.isDigit() || char == '.' }
        var seenRawChars = 0
        original.forEachIndexed { index, char ->
            if (char.isDigit() || char == '.') {
                seenRawChars += 1
            }
            if (seenRawChars == targetRawChars) {
                return index + 1
            }
        }
        return original.length
    }
}
