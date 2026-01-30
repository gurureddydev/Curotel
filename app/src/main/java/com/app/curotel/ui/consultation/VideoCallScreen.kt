package com.app.curotel.ui.consultation

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.service.agora.CallState
import com.app.curotel.ui.consultation.components.ChatOverlay
import com.app.curotel.viewmodel.ConsultationViewModel

import com.app.curotel.ui.theme.DeepSpaceBlack
import com.app.curotel.ui.theme.NeonCyan
import com.app.curotel.ui.theme.NeonRose
import com.app.curotel.ui.theme.TextWhite
import com.app.curotel.ui.theme.NeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    viewModel: ConsultationViewModel = hiltViewModel(),
    onCallEnd: () -> Unit
) {
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val remoteParticipants by viewModel.remoteParticipants.collectAsStateWithLifecycle()
    val isLocalAudioEnabled by viewModel.isLocalAudioEnabled.collectAsStateWithLifecycle()
    val isLocalVideoEnabled by viewModel.isLocalVideoEnabled.collectAsStateWithLifecycle()
    val isVitalsSharing by viewModel.isVitalsSharing.collectAsStateWithLifecycle()
    val currentVitals by viewModel.currentVitals.collectAsStateWithLifecycle()
    // val isChatVisible by viewModel.isChatVisible.collectAsStateWithLifecycle() // No longer used here locally
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DeepSpaceBlack)) {
        // Remote video (doctor) - full screen
        if (remoteParticipants.isNotEmpty()) {
            AgoraVideoSurface(
                modifier = Modifier.fillMaxSize(),
                onSurfaceReady = { surface ->
                    viewModel.setupRemoteVideo(surface, remoteParticipants.first().uid)
                }
            )
        } else {
            // Waiting for doctor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepSpaceBlack),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Waiting for doctor...", color = TextWhite)
                }
            }
        }

        // Local video (patient) - picture-in-picture
        if (isLocalVideoEnabled) {
            AgoraVideoSurface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, NeonCyan, RoundedCornerShape(12.dp)),
                onSurfaceReady = { surface ->
                    viewModel.setupLocalVideo(surface)
                }
            )
        }

        // Vitals overlay
        if (isVitalsSharing) {
            VitalsOverlay(
                vitals = currentVitals,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        // Connection status
        when (callState) {
            is CallState.Connecting -> ConnectionBanner("Connecting...")
            is CallState.Reconnecting -> ConnectionBanner("Reconnecting...")
            is CallState.Error -> ConnectionBanner("Connection error", isError = true)
            else -> {}
        }

        // Chat Bottom Sheet REMOVED - using separate screen now

        // Call controls
        CallControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            isMuted = !isLocalAudioEnabled,
            isCameraOn = isLocalVideoEnabled,
            isVitalsSharing = isVitalsSharing,
            onMuteToggle = { viewModel.toggleMute() },
            onCameraToggle = { viewModel.toggleCamera() },
            onSwitchCamera = { viewModel.switchCamera() },
            onVitalsToggle = { viewModel.toggleVitalsSharing() },
            onEndCall = {
                viewModel.endConsultation()
                onCallEnd()
            }
        )
    }
}

@Composable
fun AgoraVideoSurface(
    modifier: Modifier = Modifier,
    onSurfaceReady: (SurfaceView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).also { onSurfaceReady(it) }
        }
    )
}

@Composable
fun ConnectionBanner(text: String, isError: Boolean = false) {
    Surface(
        color = if (isError) NeonRose else NeonCyan,
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CallControls(
    modifier: Modifier = Modifier,
    isMuted: Boolean,
    isCameraOn: Boolean,
    isVitalsSharing: Boolean,
    onMuteToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onVitalsToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Mute button
        CallControlButton(
            icon = if (isMuted) "🔇" else "🔊",
            isActive = !isMuted,
            onClick = onMuteToggle
        )

        // Camera toggle
        CallControlButton(
            icon = if (isCameraOn) "📷" else "🚫",
            isActive = isCameraOn,
            onClick = onCameraToggle
        )
        
        // Chat Toggle (New) - REMOVED
        /*
        CallControlButton(
            icon = "💬",
            isActive = isChatVisible,
            onClick = onChatToggle
        )
        */

        // Switch camera
        CallControlButton(
            icon = "🔄",
            onClick = onSwitchCamera
        )

        // Share vitals
        CallControlButton(
            icon = "❤️",
            isActive = isVitalsSharing,
            onClick = onVitalsToggle
        )

        // End call
        CallControlButton(
            icon = "📵",
            color = NeonRose,
            onClick = onEndCall
        )
    }
}

@Composable
fun CallControlButton(
    icon: String,
    isActive: Boolean = false,
    color: Color = Color.Transparent,
    onClick: () -> Unit
) {
    val backgroundColor = if (color != Color.Transparent) color else if (isActive) NeonCyan else Color.White.copy(alpha = 0.2f)
    val contentColor = if (color != Color.Transparent) Color.White else if (isActive) Color.Black else Color.White
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(50.dp)
    ) {
        Text(text = icon, fontSize = 20.sp, color = contentColor)
    }
}


@Composable
fun VitalsOverlay(
    vitals: VitalsPacket,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("LIVE VITALS", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))

            vitals.heartRate?.let {
                VitalRow("❤️ HR", "$it BPM", NeonRose)
            }
            vitals.spo2?.let {
                VitalRow("💨 SpO2", "$it%", NeonCyan)
            }
            vitals.temperature?.let {
                VitalRow("🌡️ Temp", "${String.format("%.1f", it)}°C", NeonLime)
            }
        }
    }
}

@Composable
fun VitalRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, modifier = Modifier.width(70.dp))
        Text(value, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
