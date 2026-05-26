package com.rimapps.arqtest.presentation.exchange.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rimapps.arqtest.presentation.exchange.model.CurrencyUiModel

@Composable
fun CurrencyListItem(
    currency: CurrencyUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .semantics {
                contentDescription = "${currency.code} currency"
            }
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = currency.flagResId),
                contentDescription = "${currency.code} flag",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = currency.code,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.ExtraBold
        )
        Canvas(modifier = Modifier.size(24.dp)) {
            if (isSelected) {
                drawCircle(color = primaryColor)
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.30f, size.height * 0.52f),
                    end = Offset(size.width * 0.45f, size.height * 0.68f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.45f, size.height * 0.68f),
                    end = Offset(size.width * 0.72f, size.height * 0.34f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            } else {
                drawCircle(
                    color = outlineColor,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}
