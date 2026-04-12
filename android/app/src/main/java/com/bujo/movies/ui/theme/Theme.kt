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
// Strictly restricted to the approved palette. Do NOT introduce any other
// colors in screens — compose them from these tokens only.
//
// Primary — in order of dominance:
//   1. BrandPink      (#F24E86)  — app background / dominant surface
//   2. BrandOffWhite  (#F8F8F8)  — text / cards / CTA container
//   3. BrandDark      (#383838)  — body text / strong labels / CTA text
//   4. BrandCyan      (#02B4FF)  — secondary CTA / links / highlights
val BrandPink = Color(0xFFF24E86)        // #1 dominant — primary bg / brand
val BrandOffWhite = Color(0xFFF8F8F8)    // #2 — text / cards / CTA container
val BrandDark = Color(0xFF383838)        // #3 — body text / CTA content
val BrandCyan = Color(0xFF02B4FF)        // #4 — secondary CTA / links

// Secondary — use only when needed (coin glyphs, status signals, etc.)
val BrandGray = Color(0xFFB1B1B1)        // neutral gray (dividers, placeholders)
val BrandGreen = Color(0xFF54D584)       // success signal
val BrandYellow = Color(0xFFF4CF54)      // coin / caution signal
val BrandLavender = Color(0xFFA1A4FD)    // highlight
val BrandRed = Color(0xFFFF5C5C)         // error / destructive
val BrandTeal = Color(0xFF35525E)        // deep teal

// ── Back-compat aliases ──────────────────────────────────────────────────────
// These names are referenced across the codebase from the previous palette.
// They now all map onto the approved tokens above so existing code keeps
// compiling without color drift.
val BrandDeepRed = BrandRed
val BrandCream = BrandOffWhite
val BrandBrown = BrandGray
val BrandGold = BrandYellow

// ── Legacy v0.1 palette ──────────────────────────────────────────────────────
// Used by screens that have been reverted to match the v0.1 APK look
// (cream background + crimson red accents). These are intentionally NOT part
// of the Brand* palette — they exist to match the shipping APK until each
// screen is re-themed to the Brand* tokens one by one.
val LegacyCream = Color(0xFFF5EBD1)      // cream background
val LegacyCrimson = Color(0xFFC8102E)    // primary red (titles, buttons)
val LegacyDark = Color(0xFF1E1E1E)       // body text
val LegacyMuted = Color(0xFF6B6B6B)      // muted secondary text

private val BujoColorScheme = lightColorScheme(
    // Primary CTA surfaces: OffWhite container with Pink label reads as the
    // natural inverse of the pink background.
    primary = BrandOffWhite,
    onPrimary = BrandPink,
    primaryContainer = BrandOffWhite,
    onPrimaryContainer = BrandPink,
    // Secondary = dark charcoal for high-contrast labels / outlined buttons.
    secondary = BrandDark,
    onSecondary = BrandOffWhite,
    secondaryContainer = BrandDark,
    onSecondaryContainer = BrandOffWhite,
    // Tertiary = cyan for hint / link accents.
    tertiary = BrandCyan,
    onTertiary = BrandOffWhite,
    // The whole app sits on pink.
    background = BrandPink,
    onBackground = BrandOffWhite,
    surface = BrandPink,
    onSurface = BrandOffWhite,
    surfaceVariant = BrandOffWhite,
    onSurfaceVariant = BrandDark,
    outline = BrandOffWhite,
    error = BrandRed,
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
