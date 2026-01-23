package com.app.curotel.ui.otoscope

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.curotel.core.ui.CyberButton
import com.app.curotel.core.ui.SectionHeader
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.theme.NeonCyan
import com.app.curotel.ui.theme.NeonLime
import com.app.curotel.ui.theme.NeonRose

@Composable
fun OtoscopeScreen(
    vitals: VitalsPacket,
    isMeasuring: Boolean,
    onStartMeasure: () -> Unit,
    onStopMeasure: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("Digital Otoscope")

        // Camera Feed Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .border(1.dp, if (isMeasuring) NeonLime else Color.DarkGray, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isMeasuring) {
                SimulatedCameraFeed()
                
                // Overlays
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Rec Indicator
                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(NeonRose.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REC", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Crosshair
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .border(width = 1.dp, color = NeonCyan.copy(alpha = 0.5f), shape = CircleShape)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Camera Off",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("FEED OFFLINE", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberButton(
                text = if (isMeasuring) "STOP FEED" else "START FEED",
                icon = if (isMeasuring) Icons.Default.Close else Icons.Default.PlayArrow,
                isDestructive = isMeasuring,
                color = NeonLime,
                onClick = {
                     if (isMeasuring) {
                        onStopMeasure()
                        isRecording = false
                    } else {
                        onStartMeasure()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SimulatedCameraFeed() {
    val infiniteTransition = rememberInfiniteTransition()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723), Color.Black),
                    radius = 800f
                )
            )
    )
}
