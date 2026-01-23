package com.app.curotel.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Modifier that adds a scale animation on press
 */
fun Modifier.scaleOnPress(
    pressedScale: Float = 0.95f,
    animationDuration: Int = 100
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "scaleOnPress"
    )
    
    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Modifier that adds a shimmer loading effect
 */
fun Modifier.shimmerEffect(
    isLoading: Boolean = true,
    shimmerColor: Color = Color.White.copy(alpha = 0.3f)
): Modifier = composed {
    if (!isLoading) return@composed this
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    this.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                shimmerColor,
                Color.Transparent
            ),
            start = Offset(translateAnimation, 0f),
            end = Offset(translateAnimation + 500f, 0f)
        )
    )
}

/**
 * Modifier for pulsing glow effect on live data
 */
fun Modifier.pulseGlow(
    color: Color,
    isActive: Boolean = true,
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 1f
): Modifier = composed {
    if (!isActive) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    this.graphicsLayer {
        this.alpha = alpha
    }
}

/**
 * Composable for a pulsing dot indicator
 */
@Composable
fun PulsingDot(
    color: Color,
    size: Int = 8,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingDotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingDotAlpha"
    )
    
    Box(
        modifier = modifier
            .size(size.dp)
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .background(color, CircleShape)
    )
}

/**
 * Modifier for fade-in animation on appear
 */
fun Modifier.fadeInOnAppear(
    durationMillis: Int = 500,
    delayMillis: Int = 0
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "fadeIn"
    )
    
    this.graphicsLayer { this.alpha = alpha }
}

/**
 * Modifier for slide-up animation on appear
 */
fun Modifier.slideUpOnAppear(
    durationMillis: Int = 400,
    delayMillis: Int = 0,
    offsetY: Float = 50f
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else offsetY,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "slideUp"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "slideUpAlpha"
    )
    
    this.graphicsLayer {
        translationY = animatedOffsetY
        this.alpha = alpha
    }
}

/**
 * Modifier for breathing/heartbeat animation
 */
fun Modifier.breathingAnimation(
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    durationMillis: Int = 2000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )
    
    this.scale(scale)
}
