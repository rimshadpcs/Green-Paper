package com.rimapps.arqtest.data.mapper

import com.rimapps.arqtest.data.local.CachedExchangeRate
import com.rimapps.arqtest.domain.model.ExchangeRate
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class CachedExchangeRateMapperTest {
    @Test
    fun `cached rate maps back to ExchangeRate`() {
        val rate = CachedExchangeRate(
            baseCurrencyCode = "USDc",
            quoteCurrencyCode = "MXN",
            bid = "18.4069700000",
            ask = "18.4105000000",
            updatedAt = "2025-10-20T20:14:57.361483956"
        ).toDomain()

        assertEquals("USDc", rate.baseCurrencyCode)
        assertEquals("MXN", rate.quoteCurrencyCode)
        assertBigDecimalEquals("18.4069700000", rate.bid)
        assertBigDecimalEquals("18.4105000000", rate.ask)
        assertEquals("2025-10-20T20:14:57.361483956", rate.updatedAt)
    }

    @Test
    fun `ExchangeRate maps to cached rate`() {
        val cachedRate = ExchangeRate(
            baseCurrencyCode = "USDc",
            quoteCurrencyCode = "ARS",
            bid = BigDecimal("1539.4290300000"),
            ask = BigDecimal("1551.0000000000"),
            updatedAt = "2025-10-21T09:44:18.512194175"
        ).toCached()

        assertEquals("USDc", cachedRate.baseCurrencyCode)
        assertEquals("ARS", cachedRate.quoteCurrencyCode)
        assertEquals("1539.4290300000", cachedRate.bid)
        assertEquals("1551.0000000000", cachedRate.ask)
        assertEquals("2025-10-21T09:44:18.512194175", cachedRate.updatedAt)
    }

    private fun assertBigDecimalEquals(
        expected: String,
        actual: BigDecimal
    ) {
        assertEquals(BigDecimal(expected).stripTrailingZeros(), actual.stripTrailingZeros())
    }
}
