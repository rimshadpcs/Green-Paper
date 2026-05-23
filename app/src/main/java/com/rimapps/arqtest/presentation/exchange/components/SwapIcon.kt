package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SwapIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val centerX = size.width / 2f
        val topY = size.height * 0.22f
        val bottomY = size.height * 0.72f

        drawLine(
            color = color,
            start = Offset(centerX, topY),
            end = Offset(centerX, bottomY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, bottomY),
            end = Offset(size.width * 0.30f, size.height * 0.52f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, bottomY),
            end = Offset(size.width * 0.70f, size.height * 0.52f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
