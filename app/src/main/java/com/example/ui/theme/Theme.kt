package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class LiquidGlassTokens(
    val isDark: Boolean,
    val backgroundBase: Color,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val glassBorder: Color,
    val glassSpecularTop: Color,
    val glowCyan: Color,
    val glowViolet: Color,
    val glowBlue: Color,
    val subtleText: Color
)

val DarkGlassTokens = LiquidGlassTokens(
    isDark = true,
    backgroundBase = ObsidianDeep,
    glassSurface = Color(0xB30F1730),
    glassSurfaceElevated = Color(0xCC172347),
    glassBorder = Color(0x3DFFFFFF),
    glassSpecularTop = Color(0x52FFFFFF),
    glowCyan = Color(0x5500E5FF),
    glowViolet = Color(0x558B5CF6),
    glowBlue = Color(0x553B82F6),
    subtleText = TextTertiaryDark
)

val LightGlassTokens = LiquidGlassTokens(
    isDark = false,
    backgroundBase = FrostLightBg,
    glassSurface = Color(0xCCFFFFFF),
    glassSurfaceElevated = Color(0xEBFFFFFF),
    glassBorder = Color(0x4D3B82F6),
    glassSpecularTop = Color(0xE6FFFFFF),
    glowCyan = Color(0x3300B8D4),
    glowViolet = Color(0x337C3AED),
    glowBlue = Color(0x332563EB),
    subtleText = TextTertiaryLight
)

val LocalLiquidGlass = staticCompositionLocalOf { DarkGlassTokens }

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = Color(0xFF132A4A),
    onPrimaryContainer = Color(0xFFB3F4FF),
    secondary = DeepViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281D4D),
    onSecondaryContainer = Color(0xFFE0D4FF),
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = ObsidianDeep,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0x3DFFFFFF),
    error = RoseError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = DeepViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = FrostLightBg,
    onBackground = TextPrimaryLight,
    surface = FrostLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = FrostLightElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x333B82F6),
    error = RoseError,
    onError = Color.White
)

val VideonShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VideonStudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassTokens = if (darkTheme) DarkGlassTokens else LightGlassTokens

    CompositionLocalProvider(LocalLiquidGlass provides glassTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VideonTypography,
            shapes = VideonShapes,
            content = content
        )
    }
}
