package com.app.curotel.ui.consult

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.scaleOnPress
import com.app.curotel.ui.theme.*

@Composable
fun ConsultScreen() {
    var isInCall by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "VIRTUAL CONSULTATION",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Text(
            text = "Connect with your healthcare provider",
            style = MaterialTheme.typography.bodySmall,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!isInCall) {
            // Pre-call state
            PreCallContent(onStartCall = { isInCall = true })
        } else {
            // In-call state
            InCallContent(onEndCall = { isInCall = false })
        }
    }
}

@Composable
fun PreCallContent(onStartCall: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Doctor avatar placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(GlassSurface)
                .border(2.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👨‍⚕️",
                style = MaterialTheme.typography.displayMedium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Dr. Sarah Johnson",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
        
        Text(
            text = "General Physician",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NeonLime, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Available Now",
                style = MaterialTheme.typography.labelMedium,
                color = NeonLime
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Start call button
        Button(
            onClick = onStartCall,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .scaleOnPress(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepSpaceBlack
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "📹 START VIDEO CALL",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "As easy as a WhatsApp call",
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
    }
}

@Composable
fun InCallContent(onEndCall: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video preview area
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👨‍⚕️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Dr. Sarah Johnson",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "00:45",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonLime
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Live vitals being shared
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SHARING LIVE DATA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    VitalChip("SpO₂", "98%", NeonRose)
                    VitalChip("HR", "72 BPM", NeonRose)
                    VitalChip("Temp", "36.8°C", NeonCyan)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Call controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mute button
            CallControlButton(
                icon = "🔇",
                label = "Mute",
                color = GlassSurface,
                onClick = {}
            )
            
            // End call button
            CallControlButton(
                icon = "📵",
                label = "End",
                color = NeonRose,
                onClick = onEndCall
            )
            
            // Camera button
            CallControlButton(
                icon = "📷",
                label = "Camera",
                color = GlassSurface,
                onClick = {}
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun VitalChip(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
    }
}

@Composable
fun CallControlButton(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier
                .size(64.dp)
                .scaleOnPress()
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
    }
}
