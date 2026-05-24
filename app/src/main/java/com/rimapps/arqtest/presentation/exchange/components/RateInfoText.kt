package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rimapps.arqtest.presentation.exchange.ExchangeUiState
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = RateInfoState(
                    rateText = currentRate?.let { rate ->
                        "1 ${ExchangeUiState.BASE_CURRENCY} = ${rate.toRateDisplay()} $selectedCurrencyCode"
                    } ?: "Rate unavailable for $selectedCurrencyCode",
                    helperText = if (currentRate == null) {
                        "Try another currency or refresh rates."
                    } else {
                        null
                    }
                ),
                transitionSpec = {
                    fadeIn(tween(RATE_TEXT_ANIMATION_MS)) togetherWith
                        fadeOut(tween(RATE_TEXT_ANIMATION_MS)) using
                        SizeTransform(clip = false)
                },
                label = "rate_text_change"
            ) { rateInfo ->
                Column {
                    Text(
                        text = rateInfo.rateText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    rateInfo.helperText?.let { helperText ->
                        Text(
                            text = helperText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AccentBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh rates",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class RateInfoState(
    val rateText: String,
    val helperText: String?
)

private val AccentBlue = Color(0xFF2F7BFF)
private const val RATE_TEXT_ANIMATION_MS = 360
