package com.app.curotel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using Default SansSerif but with strict weight hierarchy for "Medical Cleanliness"
val MedicalFontFamily = FontFamily.SansSerif

val Typography = Typography(
    // Big Vitals Numbers (e.g. "98")
    displayLarge = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.Light, // Thin & Elegant
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1.5).sp
    ),
    // Secondary Vitals (e.g. "/ 80")
    displayMedium = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    // Section Headers (e.g. "Patient Vitals")
    titleLarge = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Card Titles (e.g. "Body Temperature")
    titleMedium = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Body Text
    bodyLarge = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Captions / Units (e.g. "BPM", "°C")
    labelSmall = TextStyle(
        fontFamily = MedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp // Uppercase look
    )
)