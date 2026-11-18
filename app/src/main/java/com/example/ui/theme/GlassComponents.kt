package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_mesh")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.backgroundBase)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowBlue, Color.Transparent),
                        center = Offset(w * (0.15f + 0.15f * drift), h * 0.12f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * (0.15f + 0.15f * drift), h * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowViolet, Color.Transparent),
                        center = Offset(w * (0.85f - 0.12f * drift), h * 0.35f),
                        radius = w * 0.68f
                    ),
                    radius = w * 0.68f,
                    center = Offset(w * (0.85f - 0.12f * drift), h * 0.35f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowCyan, Color.Transparent),
                        center = Offset(w * (0.35f + 0.25f * drift), h * 0.85f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * (0.35f + 0.25f * drift), h * 0.85f)
                )
            },
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevated: Boolean = false,
    accentGlow: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val surfaceColor = if (elevated) tokens.glassSurfaceElevated else tokens.glassSurface
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentGlow?.copy(alpha = 0.75f) ?: tokens.glassSpecularTop,
            tokens.glassBorder,
            accentGlow?.copy(alpha = 0.25f) ?: tokens.glassBorder.copy(alpha = 0.15f)
        )
    )

    val clickMod = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(clickMod)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = (surfaceColor.alpha + 0.08f).coerceAtMost(0.95f)),
                        surfaceColor
                    )
                ),
                shape = shape
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "gradient_primary_button"
) {
    val brush = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(ElectricBlue, DeepViolet, NeonCyan.copy(alpha = 0.85f))
        } else {
            listOf(SlateMuted.copy(alpha = 0.4f), SlateMuted.copy(alpha = 0.3f))
        }
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.55f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush, shape = RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GlassStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    dot: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dot) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val tokens = LocalLiquidGlass.current
    val bgColor = if (selected) {
        Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.35f), DeepViolet.copy(alpha = 0.35f)))
    } else {
        Brush.horizontalGradient(listOf(tokens.glassSurface, tokens.glassSurface))
    }
    val borderColor = if (selected) NeonCyan else tokens.glassBorder

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LocalAiNoticeBanner(
    isIndonesian: Boolean,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        accentGlow = AmberWarning,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = AmberWarning,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isIndonesian) {
                        "Mode Studio Lokal — AI Cloud Memerlukan Firebase"
                    } else {
                        "Local Studio Mode — Cloud AI Requires Firebase"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = AmberWarning
                )
                Text(
                    text = if (isIndonesian) {
                        "Asisten Prompt Gemini & AI Image Cloud menampilkan respons sampel karena Firebase tidak diaktifkan. Render Video MP4, Audio TTS/SFX, Video Editor & Adapter Self-Hosted berjalan penuh di perangkat."
                    } else {
                        "Gemini Prompt Assistant & Cloud AI Image show labeled sample responses because Firebase was not enabled. Local MP4 Video Render, TTS/SFX Audio, Video Editor & Self-Hosted Adapters run natively on-device."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Rounded.AutoAwesome,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(ElectricBlue.copy(alpha = 0.25f), DeepViolet.copy(alpha = 0.25f)))
                    )
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (action != null) {
            action()
        }
    }
}
