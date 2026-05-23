package com.rimapps.arqtest.presentation.exchange.components

import android.graphics.fonts.Font
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.rimapps.arqtest.presentation.exchange.ExchangeUiState
import com.rimapps.arqtest.presentation.util.toRateDisplay
import java.math.BigDecimal

@Composable
fun RateInfoText(
    currentRate: BigDecimal?,
    selectedCurrencyCode: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = currentRate?.let { rate ->
            "1 ${ExchangeUiState.BASE_CURRENCY} = ${rate.toRateDisplay()} $selectedCurrencyCode"
        } ?: "Rate unavailable",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
    )
}
