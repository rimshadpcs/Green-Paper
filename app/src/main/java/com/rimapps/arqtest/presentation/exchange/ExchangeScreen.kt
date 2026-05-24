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
import androidx.compose.runtime.getValue
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
import com.rimapps.arqtest.domain.model.Currency
import com.rimapps.arqtest.presentation.exchange.components.CurrencyAmountCard
import com.rimapps.arqtest.presentation.exchange.components.CurrencyCardCutout
import com.rimapps.arqtest.presentation.exchange.components.CurrencyPickerBottomSheet
import com.rimapps.arqtest.presentation.exchange.components.ExchangeErrorBanner
import com.rimapps.arqtest.presentation.exchange.components.RateInfoText
import com.rimapps.arqtest.presentation.exchange.components.SwapButton
import java.math.BigDecimal

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
                    Spacer(modifier = Modifier.height(72.dp))
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
                            amount = state.topAmount,
                            amountInputField = AmountInputField.Top,
                            isCurrencySelectable = state.topCurrencyCode != ExchangeUiState.BASE_CURRENCY,
                            cutout = CurrencyCardCutout.Bottom,
                            autoFocus = true,
                            onCurrencyClick = { onEvent(ExchangeUiEvent.CurrencyPickerOpened) },
                            onAmountChange = { field, value ->
                                onEvent(ExchangeUiEvent.AmountChanged(field, value))
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
                            amount = state.bottomAmount,
                            amountInputField = AmountInputField.Bottom,
                            isCurrencySelectable = state.bottomCurrencyCode != ExchangeUiState.BASE_CURRENCY,
                            cutout = CurrencyCardCutout.Top,
                            onCurrencyClick = { onEvent(ExchangeUiEvent.CurrencyPickerOpened) },
                            onAmountChange = { field, value ->
                                onEvent(ExchangeUiEvent.AmountChanged(field, value))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        )

                        SwapButton(
                            onClick = { onEvent(ExchangeUiEvent.SwapClicked) },
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
                    Currency(code = "MXN"),
                    Currency(code = "ARS"),
                    Currency(code = "BRL"),
                    Currency(code = "COP")
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
    Currency(code = "MXN"),
    Currency(code = "ARS"),
    Currency(code = "BRL"),
    Currency(code = "COP")
)
