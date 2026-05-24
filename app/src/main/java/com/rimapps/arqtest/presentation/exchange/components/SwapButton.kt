package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SwapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationTarget by remember { mutableIntStateOf(0) }
    val rotationDegrees by animateFloatAsState(
        targetValue = rotationTarget * FULL_ROTATION_DEGREES,
        animationSpec = tween(durationMillis = SWAP_ROTATION_DURATION_MS),
        label = "swap_icon_rotation"
    )
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            IconButton(
                onClick = {
                    rotationTarget += 1
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onClick()
                },
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .semantics { contentDescription = "Swap currencies" }
            ) {
                SwapIcon(
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotationDegrees)
                )
            }
        }
    }
}

private const val FULL_ROTATION_DEGREES = 180f
private const val SWAP_ROTATION_DURATION_MS = 300
