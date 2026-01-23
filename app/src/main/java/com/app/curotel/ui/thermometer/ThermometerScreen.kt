package com.app.curotel.ui.thermometer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.curotel.core.ui.CyberButton
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.NeonText
import com.app.curotel.core.ui.SectionHeader
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.theme.NeonCyan
import com.app.curotel.ui.theme.NeonLime

@Composable
fun ThermometerScreen(
    vitals: VitalsPacket,
    onStartMeasure: () -> Unit,
    onStopMeasure: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        
        SectionHeader("Body Temperature")

        // Main Card
        GlassCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NeonText(
                        text = vitals.temperature?.let { String.format("%.1f", it) } ?: "--.-",
                        style = MaterialTheme.typography.displayLarge,
                        color = NeonCyan
                    )
                    Text("CELSIUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action
        CyberButton(
            text = "SCAN TEMPERATURE",
            icon = Icons.Default.PlayArrow,
            onClick = onStartMeasure,
            color = NeonCyan
        )
    }
}
