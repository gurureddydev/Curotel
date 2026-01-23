package com.app.curotel.ui.bpmonitor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.CyberButton
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.NeonText
import com.app.curotel.core.ui.SectionHeader
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.theme.NeonPurple

@Composable
fun BpMonitorScreen(
    vitals: VitalsPacket,
    isMeasuring: Boolean,
    onStartMeasure: () -> Unit,
    onStopMeasure: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("Blood Pressure")

        // Main BP Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SYSTOLIC / DIASTOLIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 64.sp, fontWeight = FontWeight.Light, color = NeonPurple)) {
                            append(vitals.sysBp?.toString() ?: "--")
                        }
                        withStyle(style = SpanStyle(fontSize = 40.sp, color = Color.Gray)) {
                            append(" / ")
                        }
                        withStyle(style = SpanStyle(fontSize = 56.sp, fontWeight = FontWeight.Normal, color = Color.White)) {
                            append(vitals.diaBp?.toString() ?: "--")
                        }
                    }
                )
                Text("mmHg", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        // Pulse below it
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PULSE RATE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                NeonText(text = "${vitals.heartRate ?: "--"} BPM", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        if (isMeasuring) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = NeonPurple,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        CyberButton(
            text = if (isMeasuring) "STOP INFLATION" else "START BP CHECK",
            icon = if (isMeasuring) Icons.Default.Close else Icons.Default.PlayArrow,
            isDestructive = isMeasuring,
            color = NeonPurple,
            onClick = { if (isMeasuring) onStopMeasure() else onStartMeasure() }
        )
    }
}
