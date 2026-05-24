package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.rimapps.arqtest.presentation.util.maxDecimalPlacesForCurrency
import com.rimapps.arqtest.presentation.util.sanitizeAmountInput

@Composable
fun CurrencyAmountCard(
    currencyCode: String,
    amount: String,
    amountInputField: AmountInputField,
    isCurrencySelectable: Boolean,
    cutout: CurrencyCardCutout = CurrencyCardCutout.None,
    autoFocus: Boolean = false,
    isAmountPlaceholder: Boolean = false,
    isAmountShimmering: Boolean = false,
    isAmountRevealing: Boolean = false,
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
    val amountAlpha = remember { Animatable(1f) }

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

    LaunchedEffect(isAmountRevealing, amount) {
        if (isAmountRevealing) {
            amountAlpha.snapTo(0f)
            amountAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(AMOUNT_VALUE_REVEAL_MS)
            )
        } else {
            amountAlpha.snapTo(1f)
        }
    }

    val cardShape = currencyCardShape(cutout)

    Box(
        modifier = modifier
            .height(88.dp)
            .cardContainer(
                shape = cardShape
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                        val sanitizedInput = sanitizeAmountInput(
                            input = value.text,
                            selectionEnd = value.selection.end,
                            maxDecimalPlaces = maxDecimalPlacesForCurrency(currencyCode)
                        )

                        textFieldValue = TextFieldValue(
                            text = sanitizedInput.text,
                            selection = TextRange(sanitizedInput.selection)
                        )
                        onAmountChange(amountInputField, sanitizedInput.text)
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = amountTextColor(
                            isPlaceholder = isAmountPlaceholder,
                            isShimmering = isAmountShimmering
                        ),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    visualTransformation = AmountVisualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    placeholder = {
                        PlaceholderAmountText(isShimmering = isAmountShimmering)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = amountTextColor(isAmountPlaceholder),
                        unfocusedTextColor = amountTextColor(
                            isPlaceholder = isAmountPlaceholder,
                            isShimmering = isAmountShimmering
                        ),
                        cursorColor = CursorBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = amountAlpha.value }
                        .focusRequester(focusRequester)
                )
            }
        }
    }
}

private val CursorBlue = Color(0xFF2F7BFF)
private const val CURRENCY_LABEL_ANIMATION_MS = 360
const val PLACEHOLDER_SHIMMER_MS = 900
private const val PLACEHOLDER_SHIMMER_WIDTH = 500
private const val PLACEHOLDER_SHIMMER_AXIS_Y = 270f
private const val AMOUNT_VALUE_REVEAL_MS = 360

@Composable
private fun Modifier.cardContainer(
    shape: Shape
): Modifier {
    return clip(shape)
        .background(MaterialTheme.colorScheme.surface)
}

@Composable
private fun amountTextColor(
    isPlaceholder: Boolean,
    isShimmering: Boolean = false
): Color {
    return if (isPlaceholder) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun PlaceholderAmountText(
    isShimmering: Boolean
) {
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "placeholder_amount_wave")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PLACEHOLDER_SHIMMER_MS + PLACEHOLDER_SHIMMER_WIDTH).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PLACEHOLDER_SHIMMER_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "placeholder_amount_wave_offset"
    )
    val brush = if (isShimmering) {
        Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.30f),
                baseColor.copy(alpha = 0.50f),
                baseColor.copy(alpha = 1.00f),
                baseColor.copy(alpha = 0.50f),
                baseColor.copy(alpha = 0.30f)
            ),
            start = Offset(
                x = translateAnimation - PLACEHOLDER_SHIMMER_WIDTH,
                y = 0f
            ),
            end = Offset(
                x = translateAnimation,
                y = PLACEHOLDER_SHIMMER_AXIS_Y
            )
        )
    } else {
        null
    }

    Text(
        text = "0.00",
        color = if (isShimmering) Color.Unspecified else baseColor.copy(alpha = 0.48f),
        style = MaterialTheme.typography.titleLarge.amountPlaceholderStyle(brush),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun TextStyle.amountPlaceholderStyle(brush: Brush?): TextStyle {
    return if (brush == null) {
        copy(
            textAlign = TextAlign.End,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    } else {
        copy(
            brush = brush,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

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
