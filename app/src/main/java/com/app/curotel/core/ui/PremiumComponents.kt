package com.app.curotel.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.ui.theme.*

// -----------------------------------------------------------------------------
// GLASSMORPHISM COMPONENTS
// -----------------------------------------------------------------------------

/**
 * A frosted glass container.
 * Uses semi-transparent blurred background simulation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2A2D3E).copy(alpha = 0.6f),
                        Color(0xFF1F2029).copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

@Composable
fun NeonText(
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = NeonCyan
) {
    Text(
        text = text,
        style = style,
        color = color,
        // In a real advanced UI, you'd add Shadow(blurRadius) here for Glow effect
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextGrey,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun CyberButton(
    text: String,
    icon: ImageVector? = null,
    color: Color = NeonCyan,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val btnColor = if (isDestructive) NeonRose else color
    
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor.copy(alpha = 0.2f),
            contentColor = btnColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, btnColor.copy(alpha = 0.5f))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = btnColor)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = text.uppercase(), fontWeight = FontWeight.Bold)
    }
}
