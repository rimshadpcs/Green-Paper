package com.rimapps.arqtest.presentation.exchange

import androidx.lifecycle.viewModelScope
import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.core.network.NetworkMonitor
import com.rimapps.arqtest.presentation.exchange.model.ExchangeAmountField
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.model.ExchangeRatesResult
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
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

    private val createdViewModels = mutableListOf<ExchangeViewModel>()

    @Test
    fun `initial load success`() = runViewModelTest {
        val viewModel = viewModel()

        runCurrent()

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
    fun `initial load failure`() = runViewModelTest {
        val viewModel = viewModel(
            repository = FakeExchangeRepository(
                currenciesResult = AppResult.Error("Unable to load currencies")
            )
        )

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Unable to load currencies", state.errorMessage)
    }

    @Test
    fun `USDc amount change updates quote amount`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "10"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("10", state.topAmount)
        assertEquals("180.00", state.bottomAmount)
    }

    @Test
    fun `quote amount change updates USDc amount`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Bottom,
                value = "180"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("10", state.topAmount)
        assertEquals("180", state.bottomAmount)
    }

    @Test
    fun `trailing decimal amount is accepted while typing`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "12."
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.", state.topAmount)
        assertEquals("216.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `leading decimal point is accepted as in progress input`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "."
            )
        )

        val state = viewModel.uiState.value
        assertEquals(".", state.topAmount)
        assertEquals("", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `zero trailing decimal amount is accepted without error`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "0."
            )
        )

        val state = viewModel.uiState.value
        assertEquals("0.", state.topAmount)
        assertEquals("0.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `letters are sanitized from amount input`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "a1b2.c3"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.3", state.topAmount)
        assertEquals("221.40", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `negative amount input is sanitized to positive digits`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "-12.50"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.50", state.topAmount)
        assertEquals("225.00", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `multiple decimal separators are sanitized`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "12.3.4"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("12.34", state.topAmount)
        assertEquals("222.12", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `large amount input shows inline field error`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
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
    fun `USDc decimals are capped to six places`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Top,
                value = "1.1234567"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("1.123456", state.topAmount)
        assertEquals("20.22", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `fiat decimals are capped to two places`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(
            ExchangeUiEvent.AmountChanged(
                field = ExchangeAmountField.Bottom,
                value = "18.4097"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("1.022222", state.topAmount)
        assertEquals("18.40", state.bottomAmount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `swap clicked swaps top and bottom currencies`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(ExchangeUiEvent.SwapClicked)

        val state = viewModel.uiState.value
        assertEquals("MXN", state.topCurrencyCode)
        assertEquals("USDc", state.bottomCurrencyCode)
    }

    @Test
    fun `currency selected updates selected currency`() = runViewModelTest {
        val viewModel = viewModel()
        runCurrent()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("ARS"))

        val state = viewModel.uiState.value
        assertEquals("ARS", state.selectedCurrencyCode)
        assertEquals("ARS", state.bottomCurrencyCode)
        assertBigDecimalEquals("1500.00", state.currentRate)
    }

    @Test
    fun `missing selected rate shows error state`() = runViewModelTest {
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
        runCurrent()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("COP"))

        val state = viewModel.uiState.value
        assertEquals("COP", state.selectedCurrencyCode)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage.orEmpty().contains("COP"))
    }

    @Test
    fun `cached rates result sets cached state`() = runViewModelTest {
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
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isUsingCachedRates)
        assertBigDecimalEquals("18.00", state.currentRate)
    }

    @Test
    fun `coming online refreshes cached rates automatically`() = runViewModelTest {
        val networkMonitor = FakeNetworkMonitor()
        val viewModel = viewModel(
            repository = CachedThenFreshExchangeRepository(),
            networkMonitor = networkMonitor
        )
        runCurrent()
        assertTrue(viewModel.uiState.value.isUsingCachedRates)

        networkMonitor.emitOnline(true)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isUsingCachedRates)
        assertBigDecimalEquals("20.00", state.currentRate)
    }

    @Test
    fun `refresh preserves selected currency and swapped card order`() = runViewModelTest {
        val viewModel = viewModel(
            repository = RefreshingExchangeRepository()
        )
        runCurrent()

        viewModel.onEvent(ExchangeUiEvent.CurrencySelected("ARS"))
        viewModel.onEvent(ExchangeUiEvent.SwapClicked)
        viewModel.onEvent(ExchangeUiEvent.RefreshClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("ARS", state.selectedCurrencyCode)
        assertEquals("ARS", state.topCurrencyCode)
        assertEquals("USDc", state.bottomCurrencyCode)
        assertBigDecimalEquals("1600.00", state.currentRate)
    }

    private fun viewModel(
        repository: ExchangeRepository = FakeExchangeRepository(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor()
    ): ExchangeViewModel {
        return ExchangeViewModel(
            exchangeRepository = repository,
            amountProcessor = ExchangeAmountProcessor(ConvertCurrencyUseCase()),
            networkMonitor = networkMonitor
        ).also { viewModel ->
            createdViewModels += viewModel
        }
    }

    private fun runViewModelTest(testBody: suspend TestScope.() -> Unit) = runTest {
        try {
            testBody()
        } finally {
            createdViewModels.forEach { viewModel ->
                viewModel.viewModelScope.cancel()
            }
            createdViewModels.clear()
        }
    }

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

    private class RefreshingExchangeRepository : ExchangeRepository {
        private var rateRequestCount = 0

        override suspend fun getAvailableCurrencies(): AppResult<List<Currency>> {
            return AppResult.Success(listOf(Currency("MXN"), Currency("ARS")))
        }

        override suspend fun getExchangeRates(currencyCodes: List<String>): AppResult<ExchangeRatesResult> {
            rateRequestCount += 1
            val arsRate = if (rateRequestCount == 1) {
                arsRate()
            } else {
                ExchangeRate(
                    baseCurrencyCode = "USDc",
                    quoteCurrencyCode = "ARS",
                    bid = BigDecimal("1590.00"),
                    ask = BigDecimal("1610.00"),
                    updatedAt = "2026-05-24T00:00:00Z"
                )
            }
            return AppResult.Success(
                ExchangeRatesResult(
                    rates = listOf(mxnRate(), arsRate),
                    isCached = false
                )
            )
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
