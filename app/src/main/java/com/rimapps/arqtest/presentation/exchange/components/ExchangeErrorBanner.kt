package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExchangeErrorBanner(
    message: String,
    isRefreshing: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val friendlyTitle = if (message.startsWith("Exchange rate unavailable")) {
        message
    } else {
        "Couldn’t load exchange rates"
    }
    val friendlyMessage = if (message.startsWith("Exchange rate unavailable")) {
        "Choose another currency or try refreshing."
    } else {
        "Check your connection and try again."
    }

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friendlyTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = friendlyMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        TextButton(
            onClick = onRetryClick,
            enabled = !isRefreshing
        ) {
            Text(text = "Retry")
        }
    }
}
