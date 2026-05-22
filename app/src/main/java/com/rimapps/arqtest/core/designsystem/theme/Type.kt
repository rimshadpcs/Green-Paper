package com.rimapps.arqtest.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.rimapps.arqtest.R

val MessinaSansMono = FontFamily(
    Font(
        resId = R.font.messina_sans_mono_light,
        weight = FontWeight.Light
    )
)

private fun messinaTextStyle(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp
) = TextStyle(
    fontFamily = MessinaSansMono,
    fontWeight = FontWeight.Light,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing
)

val ArqTypography = Typography(
    displayLarge = messinaTextStyle(fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = messinaTextStyle(fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = messinaTextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = messinaTextStyle(fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = messinaTextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = messinaTextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = messinaTextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = messinaTextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = messinaTextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = messinaTextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = messinaTextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = messinaTextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = messinaTextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = messinaTextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = messinaTextStyle(fontSize = 11.sp, lineHeight = 16.sp)
)
