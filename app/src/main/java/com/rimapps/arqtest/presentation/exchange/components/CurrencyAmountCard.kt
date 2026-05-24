package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = amount,
                selection = TextRange(amount.length)
            )
        )
    }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(amount) {
        if (amount != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = amount,
                selection = TextRange(amount.length)
            )
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
            AnimatedContent(
                targetState = CurrencyLabelState(
                    currencyCode = currencyCode,
                    isCurrencySelectable = isCurrencySelectable
                ),
                transitionSpec = {
                    fadeIn(tween(CURRENCY_LABEL_ANIMATION_MS)) togetherWith
                        fadeOut(tween(CURRENCY_LABEL_ANIMATION_MS)) using
                        SizeTransform(clip = false)
                },
                label = "currency_label_change"
            ) { labelState ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = labelState.currencyCode.flagDrawableResId()),
                        contentDescription = "${labelState.currencyCode} flag",
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = labelState.currencyCode,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                    if (labelState.isCurrencySelectable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        DownChevron(modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(1.2f),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = { value ->
                    val sanitizedAmount = value.text.sanitizeAmountInput()
                    val selection = if (sanitizedAmount == value.text) {
                        value.selection.end.coerceIn(0, sanitizedAmount.length)
                    } else {
                        sanitizedAmount.length
                    }

                    textFieldValue = TextFieldValue(
                        text = sanitizedAmount,
                        selection = TextRange(selection)
                    )
                    onAmountChange(amountInputField, sanitizedAmount)
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
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
private const val CURRENCY_LABEL_ANIMATION_MS = 360

private data class CurrencyLabelState(
    val currencyCode: String,
    val isCurrencySelectable: Boolean
)

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
