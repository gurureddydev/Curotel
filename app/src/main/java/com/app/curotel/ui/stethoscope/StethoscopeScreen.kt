package com.app.curotel.ui.stethoscope

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.scaleOnPress
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.theme.*
import kotlin.math.sin

enum class StethoscopeMode {
    HEART, LUNG
}

@Composable
fun StethoscopeScreen(
    vitals: VitalsPacket,
    isMeasuring: Boolean = false,
    onStartMeasure: () -> Unit,
    onStopMeasure: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(StethoscopeMode.HEART) }
    var volume by remember { mutableFloatStateOf(0.7f) }
    var isRecording by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        Text(
            text = "DIGITAL STETHOSCOPE",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Text(
            text = "World's most affordable Bluetooth Digital Stethoscope",
            style = MaterialTheme.typography.bodySmall,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mode Toggle (Heart / Lung)
        ModeToggle(
            selectedMode = selectedMode,
            onModeChange = { selectedMode = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Audio Visualization Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedMode == StethoscopeMode.HEART) "HEART SOUNDS" else "LUNG SOUNDS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGrey
                    )
                    
                    // Recording indicator
                    if (isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RecordingDot()
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECORDING",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonRose
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Waveform visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    StethoscopeWaveform(
                        isActive = isMeasuring,
                        mode = selectedMode,
                        color = if (selectedMode == StethoscopeMode.HEART) NeonRose else NeonCyan
                    )
                }
                
                // BPM Display (for heart mode)
                if (selectedMode == StethoscopeMode.HEART) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMeasuring) "${vitals.heartRate ?: 72}" else "--",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonRose
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGrey
                        )
                    }
                }
            }
        }
        
        // Volume Control
        VolumeControl(
            volume = volume,
            onVolumeChange = { volume = it }
        )
        
        // Filter Controls
        FilterControls()
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Record Button
            Button(
                onClick = { isRecording = !isRecording },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .scaleOnPress(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) NeonRose.copy(alpha = 0.2f) else GlassSurface,
                    contentColor = if (isRecording) NeonRose else TextWhite
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRecording) NeonRose else TextGrey.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (isRecording) "⏹ STOP REC" else "⏺ RECORD",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            
            // Listen Button
            Button(
                onClick = { if (isMeasuring) onStopMeasure() else onStartMeasure() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .scaleOnPress(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMeasuring) NeonCyan else NeonCyan.copy(alpha = 0.2f),
                    contentColor = if (isMeasuring) DeepSpaceBlack else NeonCyan
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isMeasuring) "⏹ STOP" else "▶ LISTEN",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ModeToggle(
    selectedMode: StethoscopeMode,
    onModeChange: (StethoscopeMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GlassSurface.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Heart Mode
            Surface(
                onClick = { onModeChange(StethoscopeMode.HEART) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedMode == StethoscopeMode.HEART) NeonRose.copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selectedMode == StethoscopeMode.HEART) 
                            Modifier.border(1.dp, NeonRose.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        else Modifier
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "❤️ HEART",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedMode == StethoscopeMode.HEART) NeonRose else TextGrey
                    )
                }
            }
            
            // Lung Mode
            Surface(
                onClick = { onModeChange(StethoscopeMode.LUNG) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedMode == StethoscopeMode.LUNG) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selectedMode == StethoscopeMode.LUNG) 
                            Modifier.border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        else Modifier
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🫁 LUNG",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedMode == StethoscopeMode.LUNG) NeonCyan else TextGrey
                    )
                }
            }
        }
    }
}

@Composable
fun StethoscopeWaveform(
    isActive: Boolean,
    mode: StethoscopeMode,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stethoscopeWaveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mode == StethoscopeMode.HEART) 800 else 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveformPhase"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        if (!isActive) {
            // Flat line when inactive
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }
        
        val path = Path()
        path.moveTo(0f, centerY)
        
        when (mode) {
            StethoscopeMode.HEART -> {
                // Heartbeat pattern: lub-dub waveform
                for (x in 0..width.toInt() step 2) {
                    val normalizedX = x / width
                    val t = (normalizedX * 4 + phase / (2 * Math.PI.toFloat())) % 1f
                    
                    val y = when {
                        t < 0.1f -> centerY - (height * 0.4f) * sin(t * Math.PI / 0.1f).toFloat()
                        t < 0.15f -> centerY
                        t < 0.25f -> centerY - (height * 0.25f) * sin((t - 0.15f) * Math.PI / 0.1f).toFloat()
                        else -> centerY
                    }
                    path.lineTo(x.toFloat(), y)
                }
            }
            StethoscopeMode.LUNG -> {
                // Breath pattern: smooth wave
                for (x in 0..width.toInt() step 2) {
                    val normalizedX = x / width
                    val y = centerY + sin((normalizedX * 2 * Math.PI + phase).toDouble()).toFloat() * (height / 4) *
                            (0.5f + 0.5f * sin((normalizedX * Math.PI).toDouble()).toFloat())
                    path.lineTo(x.toFloat(), y)
                }
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Glow effect
        drawPath(
            path = path,
            color = color.copy(alpha = 0.3f),
            style = Stroke(width = 8.dp.toPx())
        )
    }
}

@Composable
fun RecordingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordingAlpha"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(NeonRose.copy(alpha = alpha))
    )
}

@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VOLUME",
                style = MaterialTheme.typography.labelMedium,
                color = TextGrey
            )
            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonCyan
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = GlassSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FilterControls() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            label = "Bass Boost",
            isSelected = true,
            color = NeonPurple,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            label = "Noise Cancel",
            isSelected = true,
            color = NeonLime,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            label = "Amplify",
            isSelected = false,
            color = NeonCyan,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { /* Toggle filter */ },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else GlassSurface.copy(alpha = 0.3f),
        modifier = modifier
            .height(36.dp)
            .then(
                if (isSelected) Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) color else TextGrey
            )
        }
    }
}
