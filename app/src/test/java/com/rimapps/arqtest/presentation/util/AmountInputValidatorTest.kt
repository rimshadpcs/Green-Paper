package com.rimapps.arqtest.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountInputValidatorTest {
    @Test
    fun `empty input remains empty`() {
        val result = sanitizeAmountInput(
            input = "",
            maxDecimalPlaces = 6
        )

        assertEquals("", result.text)
        assertEquals(0, result.selection)
    }

    @Test
    fun `decimal input is preserved`() {
        val result = sanitizeAmountInput(
            input = "12.34",
            maxDecimalPlaces = 6
        )

        assertEquals("12.34", result.text)
    }

    @Test
    fun `single decimal point is allowed`() {
        val result = sanitizeAmountInput(
            input = ".",
            maxDecimalPlaces = 6
        )

        assertEquals(".", result.text)
    }

    @Test
    fun `trailing decimal point is allowed`() {
        val result = sanitizeAmountInput(
            input = "0.",
            maxDecimalPlaces = 6
        )

        assertEquals("0.", result.text)
    }

    @Test
    fun `letters are removed from pasted input`() {
        val result = sanitizeAmountInput(
            input = "a1b2.c3",
            maxDecimalPlaces = 6
        )

        assertEquals("12.3", result.text)
    }

    @Test
    fun `negative sign is removed`() {
        val result = sanitizeAmountInput(
            input = "-12.50",
            maxDecimalPlaces = 6
        )

        assertEquals("12.50", result.text)
    }

    @Test
    fun `multiple decimal separators are collapsed to the first separator`() {
        val result = sanitizeAmountInput(
            input = "12.3.4.5",
            maxDecimalPlaces = 6
        )

        assertEquals("12.345", result.text)
    }

    @Test
    fun `large integer input is preserved and marked too large`() {
        val result = sanitizeAmountInput(
            input = "9999999999999999999",
            maxDecimalPlaces = 6
        )

        assertEquals("9999999999999999999", result.text)
        assertTrue(result.isTooLarge)
    }

    @Test
    fun `reasonable integer input is not marked too large`() {
        val result = sanitizeAmountInput(
            input = "999999999999",
            maxDecimalPlaces = 6
        )

        assertEquals("999999999999", result.text)
        assertFalse(result.isTooLarge)
    }

    @Test
    fun `too many decimals are capped`() {
        val result = sanitizeAmountInput(
            input = "1.123456789",
            maxDecimalPlaces = 6
        )

        assertEquals("1.123456", result.text)
    }

    @Test
    fun `fiat decimal places are capped to two`() {
        val result = sanitizeAmountInput(
            input = "18.4097",
            maxDecimalPlaces = maxDecimalPlacesForCurrency("MXN")
        )

        assertEquals("18.40", result.text)
    }
}
