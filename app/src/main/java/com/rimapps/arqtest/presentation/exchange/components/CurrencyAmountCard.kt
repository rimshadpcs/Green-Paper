package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rimapps.arqtest.core.designsystem.theme.ArqTestTheme
import com.rimapps.arqtest.domain.model.AmountInputField
import com.rimapps.arqtest.presentation.exchange.flagDrawableResId
import com.rimapps.arqtest.presentation.util.AmountVisualTransformation

@Composable
fun CurrencyAmountCard(
    currencyCode: String,
    amount: String,
    amountInputField: AmountInputField,
    isCurrencySelectable: Boolean,
    cutout: CurrencyCardCutout = CurrencyCardCutout.None,
    autoFocus: Boolean = false,
    onCurrencyClick: () -> Unit,
    onAmountChange: (AmountInputField, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = modifier
            .height(88.dp)
            .clip(currencyCardShape(cutout))
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isCurrencySelectable) {
                        Modifier.clickable(onClick = onCurrencyClick)
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            Image(
                painter = painterResource(id = currencyCode.flagDrawableResId()),
                contentDescription = "$currencyCode flag",
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = currencyCode,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (isCurrencySelectable) {
                Spacer(modifier = Modifier.width(8.dp))
                DownChevron(modifier = Modifier.size(16.dp))
            }
        }

        Box(
            modifier = Modifier.weight(1.2f),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextField(
                value = amount,
                onValueChange = { value ->
                    onAmountChange(amountInputField, value.sanitizeAmountInput())
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.ExtraBold
                ),
                singleLine = true,
                visualTransformation = AmountVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = CursorBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
    }
}

private val CursorBlue = Color(0xFF2F7BFF)

@Composable
private fun DownChevron(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.38f),
            end = Offset(size.width * 0.50f, size.height * 0.66f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.50f, size.height * 0.66f),
            end = Offset(size.width * 0.78f, size.height * 0.38f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun String.sanitizeAmountInput(): String {
    var hasDecimal = false
    return buildString {
        this@sanitizeAmountInput.forEach { char ->
            when {
                char.isDigit() -> append(char)
                char == '.' && !hasDecimal -> {
                    append(char)
                    hasDecimal = true
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAF7)
@Composable
private fun CurrencyAmountCardPreview() {
    ArqTestTheme {
        CurrencyAmountCard(
            currencyCode = "MXN",
            amount = "184065.59",
            amountInputField = AmountInputField.Bottom,
            isCurrencySelectable = true,
            cutout = CurrencyCardCutout.Top,
            onCurrencyClick = {},
            onAmountChange = { _, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
