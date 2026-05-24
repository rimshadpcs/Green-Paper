package com.rimapps.arqtest.presentation.exchange.components

import android.media.AudioAttributes
import android.media.SoundPool
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rimapps.arqtest.R
import kotlinx.coroutines.delay

@Composable
fun SwapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationTarget by remember { mutableIntStateOf(0) }
    var isTapThrottled by remember { mutableStateOf(false) }
    val rotationDegrees by animateFloatAsState(
        targetValue = rotationTarget * FULL_ROTATION_DEGREES,
        animationSpec = tween(durationMillis = SWAP_ROTATION_DURATION_MS),
        label = "swap_icon_rotation"
    )
    val hapticFeedback = LocalHapticFeedback.current
    val clickSound = rememberClickSound()

    LaunchedEffect(rotationTarget) {
        if (rotationTarget > 0) {
            delay(SWAP_TAP_THROTTLE_MS.toLong())
            isTapThrottled = false
        }
    }

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            IconButton(
                onClick = {
                    if (isTapThrottled) return@IconButton
                    isTapThrottled = true
                    rotationTarget += 1
                    clickSound.play()
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

private const val FULL_ROTATION_DEGREES = 360f
private const val SWAP_ROTATION_DURATION_MS = 300
private const val SWAP_TAP_THROTTLE_MS = 320

@Composable
private fun rememberClickSound(): ClickSound {
    val context = LocalContext.current
    val clickSound = remember { ClickSound() }

    DisposableEffect(context) {
        clickSound.load(context = context)
        onDispose {
            clickSound.release()
        }
    }

    return clickSound
}

private class ClickSound {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isLoaded: Boolean by mutableStateOf(false)

    fun load(context: android.content.Context) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { _, loadedSoundId, status ->
                    if (loadedSoundId == soundId && status == 0) {
                        isLoaded = true
                    }
                }
                soundId = pool.load(context, R.raw.click, 1)
            }
    }

    fun play() {
        if (isLoaded) {
            soundPool?.play(soundId, CLICK_VOLUME, CLICK_VOLUME, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundId = 0
        isLoaded = false
    }

    private companion object {
        const val CLICK_VOLUME = 1f
    }
}
