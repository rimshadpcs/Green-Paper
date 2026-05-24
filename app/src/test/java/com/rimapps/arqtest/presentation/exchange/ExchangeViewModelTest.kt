package com.rimapps.arqtest.presentation.exchange

import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.core.network.NetworkMonitor
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.model.ExchangeRatesResult
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    fun `trailing decimal amount is accepted while typing`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "12."
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.", state.topAmount)
        assertEquals("216.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `leading decimal point is accepted as in progress input`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "."
            )
        )

        val state = viewModel.uiState.value
        assertEquals(".", state.topAmount)
        assertEquals("", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `zero trailing decimal amount is accepted without error`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "0."
            )
        )

        val state = viewModel.uiState.value
        assertEquals("0.", state.topAmount)
        assertEquals("0.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `letters are sanitized from amount input`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "a1b2.c3"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.3", state.topAmount)
        assertEquals("221.40", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `negative amount input is sanitized to positive digits`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "-12.50"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.50", state.topAmount)
        assertEquals("225.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `multiple decimal separators are sanitized`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "12.3.4"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.34", state.topAmount)
        assertEquals("222.12", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `large amount input shows inline field error`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "9999999999999999999"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("9999999999999999999", state.topAmount)
        assertEquals("", state.bottomAmount)
        assertEquals("Amount is too large", state.topAmountError)
        assertEquals(null, state.bottomAmountError)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `USDc decimals are capped to six places`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Top,
                value = "1.1234567"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("1.123456", state.topAmount)
        assertEquals("20.22", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `fiat decimals are capped to two places`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = AmountInputField.Bottom,
                value = "18.4097"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("1.022222", state.topAmount)
        assertEquals("18.40", state.bottomAmount)
        assertEquals(null, state.errorMessage)
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
                ratesResult = AppResult.Success(
                    ExchangeRatesResult(
                        rates = listOf(mxnRate()),
                        isCached = false
                    )
                )
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("COP"))

        val state = viewModel.uiState.value
        assertEquals("COP", state.selectedCurrencyCode)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage.orEmpty().contains("COP"))
    }

    @Test
    fun `cached rates result sets cached state`() = runTest {
        val viewModel = viewModel(
            repository = FakeExchangeRepository(
                ratesResult = AppResult.Success(
                    ExchangeRatesResult(
                        rates = listOf(mxnRate(), arsRate()),
                        isCached = true
                    )
                )
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isUsingCachedRates)
        assertBigDecimalEquals("18.00", state.currentRate)
    }

    @Test
    fun `coming online refreshes cached rates automatically`() = runTest {
        val networkMonitor = FakeNetworkMonitor()
        val viewModel = viewModel(
            repository = CachedThenFreshExchangeRepository(),
            networkMonitor = networkMonitor
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isUsingCachedRates)

        networkMonitor.emitOnline(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUsingCachedRates)
        assertBigDecimalEquals("20.00", state.currentRate)
    }

    private fun viewModel(
        repository: ExchangeRepository = FakeExchangeRepository(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor()
    ) = ExchangeViewModel(
        exchangeRepository = repository,
        convertCurrencyUseCase = ConvertCurrencyUseCase(),
        networkMonitor = networkMonitor
    )

    private class FakeExchangeRepository(
        private val currenciesResult: AppResult<List<Currency>> = AppResult.Success(
            listOf(Currency("MXN"), Currency("ARS"))
        ),
        private val ratesResult: AppResult<ExchangeRatesResult> = AppResult.Success(
            ExchangeRatesResult(
                rates = listOf(
                    mxnRate(),
                    arsRate()
                ),
                isCached = false
            )
        )
    ) : ExchangeRepository {
        override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
            return currenciesResult
        }

        override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<ExchangeRatesResult> {
            return ratesResult
        }
    }

    private class CachedThenFreshExchangeRepository : ExchangeRepository {
        private var rateRequestCount = 0

        override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
            return AppResult.Success(listOf(Currency("MXN")))
        }

        override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<ExchangeRatesResult> {
            rateRequestCount += 1
            return if (rateRequestCount == 1) {
                AppResult.Success(
                    ExchangeRatesResult(
                        rates = listOf(mxnRate()),
                        isCached = true
                    )
                )
            } else {
                AppResult.Success(
                    ExchangeRatesResult(
                        rates = listOf(
                            ExchangeRate(
                                baseCurrencyCode = "USDc",
                                quoteCurrencyCode = "MXN",
                                bid = BigDecimal("19.00"),
                                ask = BigDecimal("21.00"),
                                updatedAt = "2026-05-24T00:00:00Z"
                            )
                        ),
                        isCached = false
                    )
                )
            }
        }
    }

    private class FakeNetworkMonitor : NetworkMonitor {
        private val onlineEvents = MutableSharedFlow<Boolean>()

        override val isOnline = onlineEvents.asSharedFlow()

        suspend fun emitOnline(isOnline: Boolean) {
            onlineEvents.emit(isOnline)
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
