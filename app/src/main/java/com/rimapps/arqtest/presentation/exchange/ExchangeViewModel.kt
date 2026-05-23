package com.rimapps.arqtest.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.arqtest.core.common.AppResult
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.domain.model.ConversionDirection
import com.rimapps.arqtest.domain.model.CurrencyAmount
import com.rimapps.arqtest.domain.model.ExchangeRate
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import com.rimapps.arqtest.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExchangeUiState(isLoading = true))
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

    private var lastEditedField: AmountInputField = AmountInputField.Top

    init {
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

    private fun loadExchangeData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = !isRefresh,
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

            val selectedCurrencyCode = selectCurrencyCode(
                availableCurrencyCodes = availableCurrencies.map { currency -> currency.code },
                currentCurrencyCode = _uiState.value.selectedCurrencyCode
            )

            _uiState.update { state ->
                state.copy(
                    availableCurrencies = availableCurrencies,
                    selectedCurrencyCode = selectedCurrencyCode,
                    bottomCurrencyCode = if (state.topCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
                        selectedCurrencyCode
                    } else {
                        ExchangeUiState.BASE_CURRENCY
                    },
                    topCurrencyCode = if (state.topCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
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

    private suspend fun loadExchangeRates(
        currencyCodes: List<String>,
        selectedCurrencyCode: String
    ) {
        val requestedCodes = currencyCodes.ifEmpty { listOf(selectedCurrencyCode) }
        when (val ratesResult = exchangeRepository.getExchangeRates(requestedCodes)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    val selectedRate = ratesResult.data.rateFor(selectedCurrencyCode)
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        exchangeRates = ratesResult.data,
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
                        errorMessage = ratesResult.message
                    )
                }
            }
        }
    }

    private fun onAmountChanged(
        field: AmountInputField,
        value: String
    ) {
        lastEditedField = field
        val state = uiState.value

        if (value.isEmpty()) {
            _uiState.update { current ->
                when (field) {
                    AmountInputField.Top -> current.copy(topAmount = "", bottomAmount = "", errorMessage = null)
                    AmountInputField.Bottom -> current.copy(topAmount = "", bottomAmount = "", errorMessage = null)
                }
            }
            return
        }

        val amount = value.toValidAmountOrNull()
        if (amount == null) {
            _uiState.update { current ->
                when (field) {
                    AmountInputField.Top -> current.copy(topAmount = value, errorMessage = "Enter a valid amount")
                    AmountInputField.Bottom -> current.copy(bottomAmount = value, errorMessage = "Enter a valid amount")
                }
            }
            return
        }

        val selectedRate = state.exchangeRates.rateFor(state.selectedCurrencyCode)
        if (selectedRate == null) {
            _uiState.update { current ->
                current.withAmount(field, value).copy(
                    errorMessage = buildMissingRateMessage(state.selectedCurrencyCode)
                )
            }
            return
        }

        convertAmount(
            field = field,
            value = value,
            amount = amount,
            exchangeRate = selectedRate
        )
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
                isCurrencyPickerVisible = false,
                currencySearchQuery = "",
                errorMessage = null
            )
        }
        updateSelectedRateState()
        recalculateFromLastEditedField()
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
            AmountInputField.Top -> AmountInputField.Bottom
            AmountInputField.Bottom -> AmountInputField.Top
        }
    }

    private fun recalculateFromLastEditedField() {
        val state = uiState.value
        val value = when (lastEditedField) {
            AmountInputField.Top -> state.topAmount
            AmountInputField.Bottom -> state.bottomAmount
        }

        if (value.isNotEmpty()) {
            onAmountChanged(lastEditedField, value)
        }
    }

    private fun convertAmount(
        field: AmountInputField,
        value: String,
        amount: BigDecimal,
        exchangeRate: ExchangeRate
    ) {
        val state = uiState.value
        val inputCurrencyCode = state.currencyCodeFor(field)
        val direction = if (inputCurrencyCode == ExchangeUiState.BASE_CURRENCY) {
            ConversionDirection.UsdcToQuote
        } else {
            ConversionDirection.QuoteToUsdc
        }

        when (
            val result = convertCurrencyUseCase(
                input = CurrencyAmount(
                    currencyCode = inputCurrencyCode,
                    amount = amount
                ),
                exchangeRate = exchangeRate,
                direction = direction
            )
        ) {
            is AppResult.Success -> {
                _uiState.update { current ->
                    current.withConvertedAmount(
                        editedField = field,
                        editedValue = value,
                        convertedValue = result.data.convertedAmount.toPlainString()
                    ).copy(
                        currentRate = result.data.rateUsed,
                        lastUpdated = exchangeRate.updatedAt,
                        errorMessage = null
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update { current ->
                    current.withAmount(field, value).copy(errorMessage = result.message)
                }
            }
        }
    }

    private fun updateSelectedRateState() {
        _uiState.update { state ->
            val selectedRate = state.exchangeRates.rateFor(state.selectedCurrencyCode)
            state.copy(
                currentRate = selectedRate?.midpoint,
                lastUpdated = selectedRate?.updatedAt,
                errorMessage = selectedRate.missingRateMessage(state.selectedCurrencyCode)
            )
        }
    }

    private fun selectCurrencyCode(
        availableCurrencyCodes: List<String>,
        currentCurrencyCode: String
    ): String {
        val normalizedCodes = availableCurrencyCodes.map { code -> code.uppercase() }
        return when {
            ExchangeUiState.DEFAULT_QUOTE_CURRENCY in normalizedCodes -> ExchangeUiState.DEFAULT_QUOTE_CURRENCY
            currentCurrencyCode.uppercase() in normalizedCodes -> currentCurrencyCode.uppercase()
            normalizedCodes.isNotEmpty() -> normalizedCodes.first()
            else -> ExchangeUiState.DEFAULT_QUOTE_CURRENCY
        }
    }

    private fun ExchangeUiState.currencyCodeFor(field: AmountInputField): String {
        return when (field) {
            AmountInputField.Top -> topCurrencyCode
            AmountInputField.Bottom -> bottomCurrencyCode
        }
    }

    private fun ExchangeUiState.withAmount(
        field: AmountInputField,
        value: String
    ): ExchangeUiState {
        return when (field) {
            AmountInputField.Top -> copy(topAmount = value)
            AmountInputField.Bottom -> copy(bottomAmount = value)
        }
    }

    private fun ExchangeUiState.withConvertedAmount(
        editedField: AmountInputField,
        editedValue: String,
        convertedValue: String
    ): ExchangeUiState {
        return when (editedField) {
            AmountInputField.Top -> copy(
                topAmount = editedValue,
                bottomAmount = convertedValue
            )
            AmountInputField.Bottom -> copy(
                topAmount = convertedValue,
                bottomAmount = editedValue
            )
        }
    }

    private fun List<ExchangeRate>.rateFor(currencyCode: String): ExchangeRate? {
        return firstOrNull { rate ->
            rate.quoteCurrencyCode.equals(currencyCode, ignoreCase = true)
        }
    }

    private fun ExchangeRate?.missingRateMessage(currencyCode: String): String? {
        return if (this == null) buildMissingRateMessage(currencyCode) else null
    }

    private fun buildMissingRateMessage(currencyCode: String): String {
        return "Exchange rate unavailable for ${currencyCode.uppercase()}"
    }

    private fun String.toValidAmountOrNull(): BigDecimal? {
        if (!matches(AMOUNT_PATTERN)) return null
        return runCatching { BigDecimal(this) }
            .getOrNull()
            ?.takeIf { amount -> amount >= BigDecimal.ZERO }
    }

    private companion object {
        val AMOUNT_PATTERN = Regex("""\d+(\.\d+)?|\.\d+""")
    }
}
