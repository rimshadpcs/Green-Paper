package com.rimapps.arqtest.data.mapper

import com.rimapps.arqtest.data.remote.dto.TickerDto
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TickerMapperTest {
    @Test
    fun `usdc_mxn maps to base USDc and quote MXN`() {
        val rate = tickerDto(book = "usdc_mxn").toDomain()

        assertEquals("USDc", rate.baseCurrencyCode)
        assertEquals("MXN", rate.quoteCurrencyCode)
    }

    @Test
    fun `bid and ask strings map to BigDecimal`() {
        val rate = tickerDto(
            bid = "18.4069700000",
            ask = "18.4105000000"
        ).toDomain()

        assertBigDecimalEquals("18.4069700000", rate.bid)
        assertBigDecimalEquals("18.4105000000", rate.ask)
    }

    @Test
    fun `invalid book is rejected cleanly`() {
        assertThrows(IllegalArgumentException::class.java) {
            tickerDto(book = "mxn").toDomain()
        }
    }

    private fun tickerDto(
        ask: String = "18.4105000000",
        bid: String = "18.4069700000",
        book: String = "usdc_mxn",
        date: String = "2025-10-20T20:14:57.361483956"
    ) = TickerDto(
        ask = ask,
        bid = bid,
        book = book,
        date = date
    )

    private fun assertBigDecimalEquals(
        expected: String,
        actual: BigDecimal
    ) {
        assertEquals(BigDecimal(expected).stripTrailingZeros(), actual.stripTrailingZeros())
    }
}
