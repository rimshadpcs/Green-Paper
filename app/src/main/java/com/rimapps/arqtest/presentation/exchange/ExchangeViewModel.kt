package com.rimapps.arqtest.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.arqtest.domain.common.AppResult
import com.rimapps.arqtest.core.network.NetworkMonitor
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import com.rimapps.arqtest.domain.repository.SelectedCurrencyRepository
import com.rimapps.arqtest.presentation.exchange.model.ExchangeAmountField
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository,
    private val amountProcessor: ExchangeAmountProcessor,
    private val networkMonitor: NetworkMonitor,
    private val selectedCurrencyRepository: SelectedCurrencyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExchangeUiState(isLoading = true))
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

    private var lastEditedField: ExchangeAmountField = ExchangeAmountField.Top
    private var exchangeDataJob: Job? = null
    private var exchangeRates: List<ExchangeRate> = emptyList()

    init {
        observeNetworkChanges()
        observeRateUpdates()
        loadExchangeData(isRefresh = false)
    }

    fun onEvent(event: ExchangeUiEvent) {
        when (event) {
            is ExchangeUiEvent.AmountChanged -> onAmountChanged(event.field, event.value)
            is ExchangeUiEvent.CurrencySelected -> onCurrencySelected(event.currencyCode)
            ExchangeUiEvent.SwapClicked -> onSwapClicked()
            ExchangeUiEvent.RefreshClicked -> loadExchangeData(isRefresh = true)
            ExchangeUiEvent.RetryClicked -> loadExchangeData(isRefresh = false)
            ExchangeUiEvent.CurrencyPickerOpened -> {
                _uiState.update { state -> state.copy(isCurrencyPickerVisible = true) }
            }
            ExchangeUiEvent.CurrencyPickerDismissed -> {
                _uiState.update { state ->
                    state.copy(
                        isCurrencyPickerVisible = false,
                        currencySearchQuery = ""
                    )
                }
            }
            is ExchangeUiEvent.SearchQueryChanged -> {
                _uiState.update { state -> state.copy(currencySearchQuery = event.query) }
            }
        }
    }

    private fun loadExchangeData(
        isRefresh: Boolean,
        showLoading: Boolean = true
    ) {
        if (exchangeDataJob?.isActive == true) return

        exchangeDataJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = showLoading && !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val currenciesResult = exchangeRepository.getAvailableCurrencies()
            val availableCurrencies = when (currenciesResult) {
                is AppResult.Success -> currenciesResult.data
                is AppResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = currenciesResult.message
                        )
                    }
                    return@launch
                }
            }

            val preferredSelection = preferredSelection()
            val selectedCurrencyCode = selectCurrencyCode(
                availableCurrencyCodes = availableCurrencies.map { currency -> currency.code },
                preferredCurrencyCode = preferredSelection.currencyCode
            )

            _uiState.update { state ->
                state.copy(
                    availableCurrencies = availableCurrencies.map { currency -> currency.toUiModel() },
                    selectedCurrencyCode = selectedCurrencyCode,
                    topCurrencyCode = if (preferredSelection.isQuoteCurrencyFirst) {
                        selectedCurrencyCode
                    } else {
                        ExchangeUiState.BASE_CURRENCY
                    },
                    bottomCurrencyCode = if (preferredSelection.isQuoteCurrencyFirst) {
                        ExchangeUiState.BASE_CURRENCY
                    } else {
                        selectedCurrencyCode
                    }
                )
            }

            loadExchangeRates(
                currencyCodes = availableCurrencies.map { currency -> currency.code },
                selectedCurrencyCode = selectedCurrencyCode
            )
        }
    }

    private fun observeRateUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(RATE_REFRESH_INTERVAL_MS)
                val state = uiState.value
                if (!state.isLoading && !state.isRefreshing) {
                    loadExchangeData(isRefresh = false, showLoading = false)
                }
            }
        }
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .collect { isOnline ->
                    val state = uiState.value
                    if (isOnline && state.isUsingCachedRates && !state.isRefreshing && !state.isLoading) {
                        loadExchangeData(isRefresh = true)
                    }
                }
        }
    }

    private suspend fun loadExchangeRates(
        currencyCodes: List<String>,
        selectedCurrencyCode: String
    ) {
        val requestedCodes = currencyCodes.ifEmpty { listOf(selectedCurrencyCode) }
        when (val ratesResult = exchangeRepository.getExchangeRates(requestedCodes)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    val rates = ratesResult.data.rates
                    exchangeRates = rates
                    val selectedRate = rates.rateFor(selectedCurrencyCode)
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isUsingCachedRates = ratesResult.data.isCached,
                        currentRate = selectedRate?.midpoint,
                        lastUpdated = selectedRate?.updatedAt,
                        errorMessage = selectedRate.missingRateMessage(selectedCurrencyCode)
                    )
                }
                recalculateFromLastEditedField()
            }
            is AppResult.Error -> {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isUsingCachedRates = false,
                        errorMessage = ratesResult.message
                    )
                }
            }
        }
    }

    private fun onAmountChanged(
        field: ExchangeAmountField,
        value: String
    ) {
        lastEditedField = field
        _uiState.update { state ->
            amountProcessor.processAmountChange(
                state = state.copy(activeAmountField = field),
                exchangeRates = exchangeRates,
                field = field,
                rawValue = value
            )
        }
    }

    private fun onCurrencySelected(currencyCode: String) {
        val normalizedCode = currencyCode.trim().uppercase()
        if (normalizedCode.isBlank()) return

        _uiState.update { state ->
            state.copy(
                selectedCurrencyCode = normalizedCode,
                topCurrencyCode = if (state.topCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
                    ExchangeUiState.BASE_CURRENCY
                } else {
                    normalizedCode
                },
                bottomCurrencyCode = if (state.bottomCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
                    ExchangeUiState.BASE_CURRENCY
                } else {
                    normalizedCode
                },
                errorMessage = null
            )
        }
        updateSelectedRateState()
        recalculateFromLastEditedField()
        saveSelectedCurrencyCode(normalizedCode)
    }

    private fun onSwapClicked() {
        _uiState.update { state ->
            state.copy(
                topCurrencyCode = state.bottomCurrencyCode,
                bottomCurrencyCode = state.topCurrencyCode,
                topAmount = state.bottomAmount,
                bottomAmount = state.topAmount,
                errorMessage = null
            )
        }
        lastEditedField = when (lastEditedField) {
            ExchangeAmountField.Top -> ExchangeAmountField.Bottom
            ExchangeAmountField.Bottom -> ExchangeAmountField.Top
        }
        _uiState.update { state -> state.copy(activeAmountField = lastEditedField) }
        saveSelectedQuoteCurrencyPosition(isQuoteCurrencyFirst = _uiState.value.topCurrencyCode != ExchangeUiState.BASE_CURRENCY)
    }

    private fun recalculateFromLastEditedField() {
        val state = uiState.value
        val value = when (lastEditedField) {
            ExchangeAmountField.Top -> state.topAmount
            ExchangeAmountField.Bottom -> state.bottomAmount
        }

        if (value.isNotEmpty()) {
            onAmountChanged(lastEditedField, value)
        }
    }

    private fun updateSelectedRateState() {
        _uiState.update { state ->
            val selectedRate = exchangeRates.rateFor(state.selectedCurrencyCode)
            state.copy(
                currentRate = selectedRate?.midpoint,
                lastUpdated = selectedRate?.updatedAt,
                errorMessage = selectedRate.missingRateMessage(state.selectedCurrencyCode)
            )
        }
    }

    private suspend fun preferredSelection(): PreferredSelection {
        val currentState = _uiState.value
        val currentCurrencyCode = currentState.selectedCurrencyCode
        val currentIsQuoteCurrencyFirst = currentState.topCurrencyCode != ExchangeUiState.BASE_CURRENCY
        return if (_uiState.value.availableCurrencies.isEmpty() &&
            currentCurrencyCode == ExchangeUiState.DEFAULT_QUOTE_CURRENCY
        ) {
            PreferredSelection(
                currencyCode = selectedCurrencyRepository.getSelectedCurrencyCode() ?: currentCurrencyCode,
                isQuoteCurrencyFirst = selectedCurrencyRepository.getSelectedQuoteCurrencyIsFirst()
                    ?: currentIsQuoteCurrencyFirst
            )
        } else {
            PreferredSelection(
                currencyCode = currentCurrencyCode,
                isQuoteCurrencyFirst = currentIsQuoteCurrencyFirst
            )
        }
    }

    private fun selectCurrencyCode(
        availableCurrencyCodes: List<String>,
        preferredCurrencyCode: String
    ): String {
        val normalizedCodes = availableCurrencyCodes.map { code -> code.uppercase() }
        return when {
            preferredCurrencyCode.uppercase() in normalizedCodes -> preferredCurrencyCode.uppercase()
            ExchangeUiState.DEFAULT_QUOTE_CURRENCY in normalizedCodes -> ExchangeUiState.DEFAULT_QUOTE_CURRENCY
            normalizedCodes.isNotEmpty() -> normalizedCodes.first()
            else -> ExchangeUiState.DEFAULT_QUOTE_CURRENCY
        }
    }

    private fun saveSelectedCurrencyCode(currencyCode: String) {
        if (currencyCode.equals(ExchangeUiState.BASE_CURRENCY, ignoreCase = true)) return

        viewModelScope.launch {
            selectedCurrencyRepository.saveSelectedCurrencyCode(currencyCode)
        }
    }

    private fun saveSelectedQuoteCurrencyPosition(isQuoteCurrencyFirst: Boolean) {
        viewModelScope.launch {
            selectedCurrencyRepository.saveSelectedQuoteCurrencyIsFirst(isQuoteCurrencyFirst)
        }
    }

    private data class PreferredSelection(
        val currencyCode: String,
        val isQuoteCurrencyFirst: Boolean
    )

    private companion object {
        const val RATE_REFRESH_INTERVAL_MS = 30_000L
    }
}
