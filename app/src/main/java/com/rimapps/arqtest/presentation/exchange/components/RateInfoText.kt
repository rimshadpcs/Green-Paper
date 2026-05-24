package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rimapps.arqtest.presentation.exchange.ExchangeUiState
import com.rimapps.arqtest.presentation.util.toLastUpdatedDisplay
import com.rimapps.arqtest.presentation.util.toRateDisplay
import java.math.BigDecimal

@Composable
fun RateInfoText(
    currentRate: BigDecimal?,
    selectedCurrencyCode: String,
    lastUpdated: String?,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AnimatedContent(
            targetState = currentRate?.let { rate ->
                "1 ${ExchangeUiState.BASE_CURRENCY} = ${rate.toRateDisplay()} $selectedCurrencyCode"
            } ?: "Rate unavailable for $selectedCurrencyCode",
            transitionSpec = {
                fadeIn(tween(RATE_TEXT_ANIMATION_MS)) togetherWith
                    fadeOut(tween(RATE_TEXT_ANIMATION_MS)) using
                    SizeTransform(clip = false)
            },
            label = "rate_text_change"
        ) { rateText ->
            Text(
                text = rateText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            lastUpdated.toLastUpdatedDisplay()?.let { updatedText ->
                Text(
                    text = updatedText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            TextButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing
            ) {
                Text(text = if (isRefreshing) "Refreshing" else "Refresh")
            }
        }
    }
}

private const val RATE_TEXT_ANIMATION_MS = 360
