package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun RateInfoChip(
    lastUpdated: String?,
    isUsingCachedRates: Boolean,
    modifier: Modifier = Modifier
) {
    var messageIndex by remember(isUsingCachedRates) { mutableStateOf(0) }

    val visibleMessages = rateInfoMessages(
        lastUpdated = lastUpdated,
        isUsingCachedRates = isUsingCachedRates
    )

    LaunchedEffect(isUsingCachedRates, visibleMessages.size) {
        if (visibleMessages.size <= 1) return@LaunchedEffect

        while (true) {
            visibleMessages.indices.forEach { index ->
                messageIndex = index
                delay(if (index == 0) RATE_CHIP_LABEL_MS else RATE_CHIP_DETAIL_MS)
            }
        }
    }

    Row(
        modifier = modifier
            .size(
                width = if (isUsingCachedRates) 232.dp else 190.dp,
                height = 42.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ChipBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RateInfoIcon(
            isWarning = isUsingCachedRates,
            modifier = Modifier
                .size(16.dp)
                .semantics {
                    contentDescription = if (isUsingCachedRates) {
                        "Cached rate warning"
                    } else {
                        "Rate information"
                    }
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedContent(
            targetState = visibleMessages.getOrElse(messageIndex.coerceAtMost(visibleMessages.lastIndex)) {
                visibleMessages.firstOrNull().orEmpty()
            },
            transitionSpec = {
                if (visibleMessages.size > 1) {
                    (slideInVertically(tween(RATE_CHIP_ANIMATION_MS)) { height -> height } +
                        fadeIn(tween(RATE_CHIP_ANIMATION_MS))) togetherWith
                        (slideOutVertically(tween(RATE_CHIP_ANIMATION_MS)) { height -> -height } +
                            fadeOut(tween(RATE_CHIP_ANIMATION_MS))) using
                        SizeTransform(clip = false)
                } else {
                    fadeIn(tween(RATE_CHIP_ANIMATION_MS)) togetherWith
                        fadeOut(tween(RATE_CHIP_ANIMATION_MS)) using
                        SizeTransform(clip = false)
                }
            },
            label = "rate_info_chip_text"
        ) { message ->
            Text(
                text = message,
                color = ChipContent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun rateInfoMessages(
    lastUpdated: String?,
    isUsingCachedRates: Boolean
): List<String> {
    return when {
        isUsingCachedRates -> listOfNotNull(
            "Last available rates",
            "Connect to update"
        )
        else -> listOfNotNull(
            "Last updated",
            lastUpdated.toRateChipTimestamp()
        )
    }
}

private fun String?.toRateChipTimestamp(): String? {
    if (isNullOrBlank()) return null

    return parseRateChipDate()?.let { date ->
        SimpleDateFormat("dd MMM, HH:mm", Locale.US).format(date)
    }
}

private fun String?.parseRateChipDate() = runCatching {
    if (isNullOrBlank()) return@runCatching null

    val normalizedDate = substringBefore(".")
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(normalizedDate)
}.getOrNull()

@Composable
private fun RateInfoIcon(
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = ChipContent,
            style = Stroke(width = 1.6.dp.toPx())
        )
        if (isWarning) {
            drawLine(
                color = ChipContent,
                start = Offset(size.width * 0.50f, size.height * 0.26f),
                end = Offset(size.width * 0.50f, size.height * 0.58f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = ChipContent,
                start = Offset(size.width * 0.50f, size.height * 0.74f),
                end = Offset(size.width * 0.50f, size.height * 0.75f),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = ChipContent,
                start = Offset(size.width * 0.50f, size.height * 0.28f),
                end = Offset(size.width * 0.50f, size.height * 0.52f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = ChipContent,
                start = Offset(size.width * 0.50f, size.height * 0.52f),
                end = Offset(size.width * 0.68f, size.height * 0.62f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private val ChipBackground = Color(0xFFF1F0EC)
private val ChipContent = Color(0xFF173B0B)
private const val RATE_CHIP_LABEL_MS = 1000L
private const val RATE_CHIP_DETAIL_MS = 2500L
private const val RATE_CHIP_ANIMATION_MS = 360
