package com.rimapps.arqtest.presentation.exchange

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rimapps.arqtest.core.designsystem.theme.ArqTestTheme
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.presentation.exchange.components.CurrencyAmountCard
import com.rimapps.arqtest.presentation.exchange.components.CurrencyCardCutout
import com.rimapps.arqtest.presentation.exchange.components.CurrencyPickerBottomSheet
import com.rimapps.arqtest.presentation.exchange.components.ExchangeErrorBanner
import com.rimapps.arqtest.presentation.exchange.components.PLACEHOLDER_SHIMMER_MS
import com.rimapps.arqtest.presentation.exchange.components.RateInfoChip
import com.rimapps.arqtest.presentation.exchange.components.RateInfoText
import com.rimapps.arqtest.presentation.exchange.components.SwapButton
import com.rimapps.arqtest.presentation.exchange.model.CurrencyUiModel
import java.math.BigDecimal
import kotlinx.coroutines.delay

@Composable
fun ExchangeScreen(
    viewModel: ExchangeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExchangeScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ExchangeScreenContent(
    state: ExchangeUiState,
    onEvent: (ExchangeUiEvent) -> Unit
) {
    var heldCalculatedField by remember { mutableStateOf<AmountInputField?>(null) }
    var shimmeringCalculatedField by remember { mutableStateOf<AmountInputField?>(null) }
    var revealingCalculatedField by remember { mutableStateOf<AmountInputField?>(null) }
    var previousTopCurrencyCode by remember { mutableStateOf(state.topCurrencyCode) }
    var previousBottomCurrencyCode by remember { mutableStateOf(state.bottomCurrencyCode) }
    var skipAmountFeedbackOnce by remember { mutableStateOf(false) }

    fun handleAmountChange(
        field: AmountInputField,
        value: String
    ) {
        if (value.isNotEmpty()) {
            heldCalculatedField = field.opposite()
            shimmeringCalculatedField = null
            revealingCalculatedField = null
        } else {
            heldCalculatedField = null
            shimmeringCalculatedField = null
            revealingCalculatedField = null
        }
        onEvent(ExchangeUiEvent.AmountChanged(field, value))
    }

    fun handleSwapClick() {
        skipAmountFeedbackOnce = true
        heldCalculatedField = null
        shimmeringCalculatedField = null
        revealingCalculatedField = null
        onEvent(ExchangeUiEvent.SwapClicked)
    }

    LaunchedEffect(
        state.topAmount,
        state.bottomAmount,
        state.activeAmountField,
        state.topAmountError,
        state.bottomAmountError
    ) {
        if (skipAmountFeedbackOnce) {
            skipAmountFeedbackOnce = false
            heldCalculatedField = null
            shimmeringCalculatedField = null
            revealingCalculatedField = null
            return@LaunchedEffect
        }

        val activeAmount = when (state.activeAmountField) {
            AmountInputField.Top -> state.topAmount
            AmountInputField.Bottom -> state.bottomAmount
        }
        val hasActiveError = when (state.activeAmountField) {
            AmountInputField.Top -> state.topAmountError != null
            AmountInputField.Bottom -> state.bottomAmountError != null
        }

        val calculatedField = if (activeAmount.isNotEmpty() && !hasActiveError) {
            state.activeAmountField.opposite()
        } else {
            null
        }

        if (calculatedField == null) {
            heldCalculatedField = null
            shimmeringCalculatedField = null
            revealingCalculatedField = null
            return@LaunchedEffect
        }

        delay(CALCULATED_AMOUNT_TYPING_IDLE_MS)
        heldCalculatedField = null
        shimmeringCalculatedField = calculatedField
        delay(CALCULATED_AMOUNT_SHIMMER_MS)
        if (shimmeringCalculatedField == calculatedField) {
            shimmeringCalculatedField = null
            revealingCalculatedField = calculatedField
            delay(CALCULATED_AMOUNT_REVEAL_MS)
            if (revealingCalculatedField == calculatedField) {
                revealingCalculatedField = null
            }
        }
    }

    LaunchedEffect(state.topCurrencyCode, state.bottomCurrencyCode) {
        val didSwap = state.topCurrencyCode == previousBottomCurrencyCode &&
            state.bottomCurrencyCode == previousTopCurrencyCode
        previousTopCurrencyCode = state.topCurrencyCode
        previousBottomCurrencyCode = state.bottomCurrencyCode
        if (didSwap) {
            skipAmountFeedbackOnce = true
            heldCalculatedField = null
            shimmeringCalculatedField = null
            revealingCalculatedField = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                ExchangeLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        RateInfoChip(
                            lastUpdated = state.lastUpdated,
                            isUsingCachedRates = state.isUsingCachedRates
                        )
                    }
                    Spacer(modifier = Modifier.height(52.dp))
                    Text(
                        text = "Exchange calculator",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 29.sp,
                            letterSpacing = (-2).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RateInfoText(
                        currentRate = state.currentRate,
                        selectedCurrencyCode = state.selectedCurrencyCode,
                        lastUpdated = state.lastUpdated,
                        isRefreshing = state.isRefreshing,
                        onRefreshClick = { onEvent(ExchangeUiEvent.RefreshClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.isUsingCachedRates) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CachedRatesBanner(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(28.dp))

                    state.errorMessage?.let { message ->
                        ExchangeErrorBanner(
                            message = message,
                            isRefreshing = state.isRefreshing,
                            onRetryClick = { onEvent(ExchangeUiEvent.RetryClicked) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(188.dp)
                            .animateContentSize()
                    ) {
                        state.topAmountError?.let { message ->
                            AmountFieldErrorText(
                                text = message,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 20.dp)
                                    .zIndex(2f)
                            )
                        }
                        CurrencyAmountCard(
                            currencyCode = state.topCurrencyCode,
                            amount = amountDisplayValue(
                                amount = state.topAmount,
                                field = AmountInputField.Top,
                                heldCalculatedField = heldCalculatedField,
                                shimmeringCalculatedField = shimmeringCalculatedField
                            ),
                            amountInputField = AmountInputField.Top,
                            isCurrencySelectable = state.topCurrencyCode != ExchangeUiState.BASE_CURRENCY,
                            cutout = CurrencyCardCutout.Bottom,
                            autoFocus = true,
                            isAmountPlaceholder = isAmountPlaceholder(
                                amount = state.topAmount,
                                field = AmountInputField.Top,
                                heldCalculatedField = heldCalculatedField,
                                shimmeringCalculatedField = shimmeringCalculatedField
                            ),
                            isAmountShimmering = shimmeringCalculatedField == AmountInputField.Top,
                            isAmountRevealing = revealingCalculatedField == AmountInputField.Top,
                            onCurrencyClick = { onEvent(ExchangeUiEvent.CurrencyPickerOpened) },
                            onAmountChange = { field, value ->
                                handleAmountChange(field, value)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )

                        state.bottomAmountError?.let { message ->
                            AmountFieldErrorText(
                                text = message,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 8.dp, end = 20.dp)
                                    .zIndex(2f)
                            )
                        }
                        CurrencyAmountCard(
                            currencyCode = state.bottomCurrencyCode,
                            amount = amountDisplayValue(
                                amount = state.bottomAmount,
                                field = AmountInputField.Bottom,
                                heldCalculatedField = heldCalculatedField,
                                shimmeringCalculatedField = shimmeringCalculatedField
                            ),
                            amountInputField = AmountInputField.Bottom,
                            isCurrencySelectable = state.bottomCurrencyCode != ExchangeUiState.BASE_CURRENCY,
                            cutout = CurrencyCardCutout.Top,
                            isAmountPlaceholder = isAmountPlaceholder(
                                amount = state.bottomAmount,
                                field = AmountInputField.Bottom,
                                heldCalculatedField = heldCalculatedField,
                                shimmeringCalculatedField = shimmeringCalculatedField
                            ),
                            isAmountShimmering = shimmeringCalculatedField == AmountInputField.Bottom,
                            isAmountRevealing = revealingCalculatedField == AmountInputField.Bottom,
                            onCurrencyClick = { onEvent(ExchangeUiEvent.CurrencyPickerOpened) },
                            onAmountChange = { field, value ->
                                handleAmountChange(field, value)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        )

                        SwapButton(
                            onClick = ::handleSwapClick,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .zIndex(1f)
                        )
                    }
                }
            }

            if (state.isCurrencyPickerVisible) {
                CurrencyPickerBottomSheet(
                    currencies = state.availableCurrencies,
                    selectedCurrencyCode = state.selectedCurrencyCode,
                    onCurrencySelected = { currencyCode ->
                        onEvent(ExchangeUiEvent.CurrencySelected(currencyCode))
                    },
                    onDismiss = { onEvent(ExchangeUiEvent.CurrencyPickerDismissed) }
                )
            }
        }
    }
}

private fun AmountInputField.opposite(): AmountInputField {
    return when (this) {
        AmountInputField.Top -> AmountInputField.Bottom
        AmountInputField.Bottom -> AmountInputField.Top
    }
}

private fun amountDisplayValue(
    amount: String,
    field: AmountInputField,
    heldCalculatedField: AmountInputField?,
    shimmeringCalculatedField: AmountInputField?
): String {
    return if (field == heldCalculatedField || field == shimmeringCalculatedField) {
        ""
    } else {
        amount
    }
}

private fun isAmountPlaceholder(
    amount: String,
    field: AmountInputField,
    heldCalculatedField: AmountInputField?,
    shimmeringCalculatedField: AmountInputField?
): Boolean {
    return amount.isEmpty() || field == heldCalculatedField || field == shimmeringCalculatedField
}

private const val CALCULATED_AMOUNT_TYPING_IDLE_MS = 650L
private const val CALCULATED_AMOUNT_SHIMMER_MS = PLACEHOLDER_SHIMMER_MS * 3L
private const val CALCULATED_AMOUNT_REVEAL_MS = 360L

@Composable
private fun CachedRatesBanner(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Showing cached rates. Go online to refresh.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 12.sp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun ExchangeLoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(72.dp))
        LoadingBlock(
            widthFraction = 0.92f,
            height = 36.dp,
            cornerRadius = 10.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        LoadingBlock(
            widthFraction = 0.62f,
            height = 24.dp,
            cornerRadius = 8.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LoadingBlock(
                widthFraction = 0.34f,
                height = 16.dp,
                cornerRadius = 8.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            LoadingBlock(
                widthFraction = 0.20f,
                height = 28.dp,
                cornerRadius = 14.dp
            )
        }
        Spacer(modifier = Modifier.height(26.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
        ) {
            LoadingBlock(
                widthFraction = 1f,
                height = 88.dp,
                cornerRadius = 22.dp,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            LoadingBlock(
                widthFraction = 1f,
                height = 88.dp,
                cornerRadius = 22.dp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
                    .zIndex(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            )
        }
    }
}

@Composable
private fun AmountFieldErrorText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 12.sp,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun LoadingBlock(
    widthFraction: Float,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    )
}

@Preview(showBackground = true)
@Composable
private fun ExchangeScreenUsdcTopPreview() {
    ArqTestTheme {
        ExchangeScreenContent(
            state = ExchangeUiState(
                availableCurrencies = listOf(
                    "MXN".toPreviewCurrencyUiModel(),
                    "ARS".toPreviewCurrencyUiModel(),
                    "BRL".toPreviewCurrencyUiModel(),
                    "COP".toPreviewCurrencyUiModel()
                ),
                topAmount = "10",
                bottomAmount = "184.097",
                currentRate = BigDecimal("18.4097")
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExchangeScreenQuoteTopPreview() {
    ArqTestTheme {
        ExchangeScreenContent(
            state = ExchangeUiState(
                availableCurrencies = previewCurrencies,
                selectedCurrencyCode = "COP",
                topCurrencyCode = "COP",
                bottomCurrencyCode = ExchangeUiState.BASE_CURRENCY,
                topAmount = "3890.83",
                bottomAmount = "1",
                currentRate = BigDecimal("3890.83")
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExchangeScreenLoadingPreview() {
    ArqTestTheme {
        ExchangeScreenContent(
            state = ExchangeUiState(isLoading = true),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExchangeScreenErrorPreview() {
    ArqTestTheme {
        ExchangeScreenContent(
            state = ExchangeUiState(
                availableCurrencies = previewCurrencies,
                topAmount = "15",
                bottomAmount = "",
                currentRate = null,
                errorMessage = "Couldn’t load exchange rates"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExchangeScreenBottomSheetPreview() {
    ArqTestTheme {
        ExchangeScreenContent(
            state = ExchangeUiState(
                availableCurrencies = previewCurrencies,
                topAmount = "10",
                bottomAmount = "184.097",
                currentRate = BigDecimal("18.4097"),
                isCurrencyPickerVisible = true
            ),
            onEvent = {}
        )
    }
}

private val previewCurrencies = listOf(
    "MXN".toPreviewCurrencyUiModel(),
    "ARS".toPreviewCurrencyUiModel(),
    "BRL".toPreviewCurrencyUiModel(),
    "COP".toPreviewCurrencyUiModel()
)

private fun String.toPreviewCurrencyUiModel(): CurrencyUiModel {
    return CurrencyUiModel(
        code = this,
        flagResId = flagDrawableResId()
    )
}
