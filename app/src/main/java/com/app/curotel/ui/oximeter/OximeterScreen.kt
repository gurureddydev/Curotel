package com.app.curotel.ui.oximeter

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.curotel.core.ui.CyberButton
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.NeonText
import com.app.curotel.core.ui.SectionHeader
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.theme.NeonCyan
import com.app.curotel.ui.theme.NeonRose

@Composable
fun OximeterScreen(
    vitals: VitalsPacket,
    isMeasuring: Boolean,
    onStartMeasure: () -> Unit,
    onStopMeasure: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("Pulse Oximetry")

        // Two Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text("SPO2", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonText(
                        text = vitals.spo2?.toString() ?: "--",
                        color = NeonCyan
                    )
                    Text("%", color = Color.Gray)
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text("PULSE", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonText(
                        text = vitals.heartRate?.toString() ?: "--",
                        color = NeonRose
                    )
                    Text("BPM", color = Color.Gray)
                }
            }
        }

        // Graph Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(1.dp) // Border space if needed
        ) {
            if (isMeasuring) {
                SimulatedWaveform()
            } else {
                Text(
                    "SENSOR STANDBY", 
                    color = Color.DarkGray, 
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        CyberButton(
            text = if (isMeasuring) "STOP SCAN" else "START SCAN",
            icon = if (isMeasuring) Icons.Default.Close else Icons.Default.PlayArrow,
            isDestructive = isMeasuring,
            onClick = { if (isMeasuring) onStopMeasure() else onStartMeasure() }
        )
    }
}

@Composable
fun SimulatedWaveform() {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height
        val path = Path()
        
        val points = 60
        val stepX = width / points
        
        path.moveTo(0f, height / 2)
        
        for (i in 0..points) {
            val x = i * stepX
            val normalizedX = (i.toFloat() / points) * 4 * Math.PI
            val signal = kotlin.math.sin(normalizedX + phase) + (kotlin.math.sin((normalizedX + phase) * 3) * 0.5)
            val yOffset = signal * (height / 6)
            
            path.lineTo(x, (height / 2) + yOffset.toFloat())
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(NeonCyan, NeonRose)),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
