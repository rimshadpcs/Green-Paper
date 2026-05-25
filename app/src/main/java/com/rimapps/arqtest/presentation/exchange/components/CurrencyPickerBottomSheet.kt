package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rimapps.arqtest.core.designsystem.theme.ArqTestTheme
import com.rimapps.arqtest.presentation.exchange.flagDrawableResId
import com.rimapps.arqtest.presentation.exchange.model.CurrencyUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerBottomSheet(
    currencies: List<CurrencyUiModel>,
    selectedCurrencyCode: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    fun dismissWithAnimation() {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose currency",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(
                    onClick = ::dismissWithAnimation,
                    modifier = Modifier.semantics { contentDescription = "Close currency picker" }
                ) {
                    CloseIcon(modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(vertical = 8.dp)
            ) {
                currencies.forEach { currency ->
                    CurrencyListItem(
                        currency = currency,
                        isSelected = currency.code.equals(selectedCurrencyCode, ignoreCase = true),
                        onClick = {
                            onCurrencySelected(currency.code)
                            coroutineScope.launch {
                                delay(SELECTION_FEEDBACK_DELAY_MS)
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

private const val SELECTION_FEEDBACK_DELAY_MS = 160L

@Preview(showBackground = true)
@Composable
private fun CurrencyPickerBottomSheetPreview() {
    ArqTestTheme {
        CurrencyPickerBottomSheet(
            currencies = listOf(
                CurrencyUiModel(code = "ARS", flagResId = "ARS".flagDrawableResId()),
                CurrencyUiModel(code = "COP", flagResId = "COP".flagDrawableResId()),
                CurrencyUiModel(code = "MXN", flagResId = "MXN".flagDrawableResId()),
                CurrencyUiModel(code = "BRL", flagResId = "BRL".flagDrawableResId())
            ),
            selectedCurrencyCode = "MXN",
            onCurrencySelected = {},
            onDismiss = {}
        )
    }
}

@Composable
private fun CloseIcon(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.22f),
            end = Offset(size.width * 0.78f, size.height * 0.78f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.78f, size.height * 0.22f),
            end = Offset(size.width * 0.22f, size.height * 0.78f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
