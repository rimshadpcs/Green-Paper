package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load success`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("MXN", "ARS"), state.availableCurrencies.map { it.code })
        assertEquals("MXN", state.selectedCurrencyCode)
        assertEquals("USDc", state.topCurrencyCode)
        assertEquals("MXN", state.bottomCurrencyCode)
        assertBigDecimalEquals("18.00", state.currentRate)
        assertEquals("2026-05-23T00:00:00Z", state.lastUpdated)
    }

    @Test
    fun `initial load failure`() = runTest {
        val viewModel = viewModel(
            repository = FakeExchangeRepository(
                currenciesResult = AppResult.Error("Unable to load currencies")
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Unable to load currencies", state.errorMessage)
    }

    @Test
    fun `USDc amount change updates quote amount`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "10"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("10", state.topAmount)
        assertEquals("180.00", state.bottomAmount)
    }

    @Test
    fun `quote amount change updates USDc amount`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Bottom,
                value = "180"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("10", state.topAmount)
        assertEquals("180", state.bottomAmount)
    }

    @Test
    fun `swap clicked swaps top and bottom currencies`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(ExchangeUiEvent.SwapClicked)

        val state = viewModel.uiState.value
        assertEquals("MXN", state.topCurrencyCode)
        assertEquals("USDc", state.bottomCurrencyCode)
    }

    @Test
    fun `currency selected updates selected currency`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("ARS"))

        val state = viewModel.uiState.value
        assertEquals("ARS", state.selectedCurrencyCode)
        assertEquals("ARS", state.bottomCurrencyCode)
        assertBigDecimalEquals("1500.00", state.currentRate)
    }

    @Test
    fun `missing selected rate shows error state`() = runTest {
        val viewModel = viewModel(
            repository = FakeExchangeRepository(
                currenciesResult = AppResult.Success(listOf(Currency("MXN"), Currency("COP"))),
                ratesResult = AppResult.Success(listOf(mxnRate()))
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("COP"))

        val state = viewModel.uiState.value
        assertEquals("COP", state.selectedCurrencyCode)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage.orEmpty().contains("COP"))
    }

    private fun viewModel(
        repository: ExchangeRepository = FakeExchangeRepository()
    ) = ExchangeViewModel(
        exchangeRepository = repository,
        convertCurrencyUseCase = ConvertCurrencyUseCase()
    )

    private class FakeExchangeRepository(
        private val currenciesResult: AppResult<List<Currency>> = AppResult.Success(
            listOf(Currency("MXN"), Currency("ARS"))
        ),
        private val ratesResult: AppResult<List<ExchangeRate>> = AppResult.Success(
            listOf(
                mxnRate(),
                arsRate()
            )
        )
    ) : ExchangeRepository {
        override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
            return currenciesResult
        }

        override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<List<ExchangeRate>> {
            return ratesResult
        }
    }

    private companion object {
        fun mxnRate() = ExchangeRate(
            baseCurrencyCode = "USDc",
            quoteCurrencyCode = "MXN",
            bid = BigDecimal("17.00"),
            ask = BigDecimal("19.00"),
            updatedAt = "2026-05-23T00:00:00Z"
        )

        fun arsRate() = ExchangeRate(
            baseCurrencyCode = "USDc",
            quoteCurrencyCode = "ARS",
            bid = BigDecimal("1490.00"),
            ask = BigDecimal("1510.00"),
            updatedAt = "2026-05-23T01:00:00Z"
        )

        fun assertBigDecimalEquals(
            expected: String,
            actual: BigDecimal?
        ) {
            assertEquals(BigDecimal(expected).stripTrailingZeros(), actual?.stripTrailingZeros())
        }
    }
}
