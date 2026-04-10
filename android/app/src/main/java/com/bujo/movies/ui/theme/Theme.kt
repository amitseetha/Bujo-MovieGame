package com.bujo.movies.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Bujo brand palette ────────────────────────────────────────────────────────
val BrandRed = Color(0xFFC8102E)
val BrandDeepRed = Color(0xFF9A0C22)
val BrandOffWhite = Color(0xFFFAF7F2)
val BrandCream = Color(0xFFF2EADB)
val BrandDark = Color(0xFF2B1810)
val BrandBrown = Color(0xFF6B4E3D)
val BrandGold = Color(0xFFD4A574)

private val BujoColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = BrandOffWhite,
    primaryContainer = BrandDeepRed,
    onPrimaryContainer = BrandOffWhite,
    secondary = BrandGold,
    onSecondary = BrandDark,
    background = BrandOffWhite,
    onBackground = BrandDark,
    surface = BrandCream,
    onSurface = BrandDark,
    surfaceVariant = BrandCream,
    onSurfaceVariant = BrandBrown,
    error = BrandDeepRed,
    onError = BrandOffWhite,
)

// Using system defaults until Google Fonts (Playfair Display + Poppins) are
// wired via androidx.compose.ui.text.googlefonts. For v0.1 the system serif/sans
// keeps things simple and still matches the warm palette.
private val BujoTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 48.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun BujoTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE")
    val dark = isSystemInDarkTheme() // ignored in v0.1; always light/warm
    MaterialTheme(
        colorScheme = BujoColorScheme,
        typography = BujoTypography,
        content = content,
    )
}
