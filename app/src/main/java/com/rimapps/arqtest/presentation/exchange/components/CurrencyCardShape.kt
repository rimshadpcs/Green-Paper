package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import kotlin.math.min

enum class CurrencyCardCutout {
    None,
    Bottom,
    Top
}

fun currencyCardShape(cutout: CurrencyCardCutout) = GenericShape { size, _ ->
    val radius = min(size.height * 0.25f, size.width * 0.8f)
    val notchHalfWidth = size.height * 0.20f
    val notchDepth = size.height * 0.16f
    val centerX = size.width / 2f

    moveTo(radius, 0f)
    if (cutout == CurrencyCardCutout.Top) {
        lineTo(centerX - notchHalfWidth, 0f)
        arcTo(
            rect = Rect(
                left = centerX - notchHalfWidth,
                top = -notchDepth,
                right = centerX + notchHalfWidth,
                bottom = notchDepth
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(size.width - radius, 0f)
    } else {
        lineTo(size.width - radius, 0f)
    }
    quadraticTo(size.width, 0f, size.width, radius)
    lineTo(size.width, size.height - radius)
    quadraticTo(size.width, size.height, size.width - radius, size.height)

    if (cutout == CurrencyCardCutout.Bottom) {
        lineTo(centerX + notchHalfWidth, size.height)
        arcTo(
            rect = Rect(
                left = centerX - notchHalfWidth,
                top = size.height - notchDepth,
                right = centerX + notchHalfWidth,
                bottom = size.height + notchDepth
            ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(radius, size.height)
    } else {
        lineTo(radius, size.height)
    }

    quadraticTo(0f, size.height, 0f, size.height - radius)
    lineTo(0f, radius)
    quadraticTo(0f, 0f, radius, 0f)
    close()
}
