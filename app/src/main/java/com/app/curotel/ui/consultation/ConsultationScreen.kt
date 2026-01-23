package com.app.curotel.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.core.ui.scaleOnPress
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.service.agora.CallState
import com.app.curotel.service.agora.RemoteParticipant
import com.app.curotel.ui.theme.*
import com.app.curotel.viewmodel.ConsultationUiState
import com.app.curotel.viewmodel.ConsultationViewModel
import com.app.curotel.viewmodel.DoctorInfo
import androidx.compose.runtime.collectAsState

// Required permissions for video call
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
)

/**
 * Main Consultation Screen - Container for all consultation states
 * Handles runtime permissions for Camera and Microphone
 */
@Composable
fun ConsultationScreen(
    viewModel: ConsultationViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val doctorInfo by viewModel.doctorInfo.collectAsStateWithLifecycle()
    
    // Permission state
    var permissionsGranted by remember { 
        mutableStateOf(
            REQUIRED_PERMISSIONS.all { 
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED 
            }
        ) 
    }
    var showPermissionDenied by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (permissionsGranted) {
            // Permissions granted, start the call
            viewModel.startConsultation()
        } else {
            showPermissionDenied = true
        }
    }
    
    // Function to request permissions and start call
    val startCallWithPermissions = {
        if (permissionsGranted) {
            viewModel.startConsultation()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        // Permission denied dialog
        if (showPermissionDenied) {
            PermissionDeniedDialog(
                onDismiss = { showPermissionDenied = false },
                onRequestAgain = {
                    showPermissionDenied = false
                    permissionLauncher.launch(REQUIRED_PERMISSIONS)
                }
            )
        }
        
        when (uiState) {
            is ConsultationUiState.NotConfigured -> {
                NotConfiguredScreen(onBack = onBack)
            }
            is ConsultationUiState.Idle -> {
                PreConsultationScreen(
                    doctorInfo = doctorInfo,
                    onStartConsultation = startCallWithPermissions,
                    onBack = onBack
                )
            }
            is ConsultationUiState.PreCall -> {
                PreCallScreen(
                    viewModel = viewModel,
                    onStartCall = startCallWithPermissions,
                    onBack = { viewModel.resetState() }
                )
            }
            is ConsultationUiState.WaitingForDoctor -> {
                WaitingRoomScreen(
                    doctorInfo = doctorInfo,
                    channelId = viewModel.currentChannelId.collectAsState().value,
                    onCancel = { viewModel.endConsultation() }
                )
            }
            is ConsultationUiState.InCall -> {
                VideoCallScreen(
                    viewModel = viewModel,
                    onEndCall = { viewModel.endConsultation() }
                )
            }
            is ConsultationUiState.Reconnecting -> {
                ReconnectingOverlay()
            }
            is ConsultationUiState.Error -> {
                ErrorScreen(
                    message = (uiState as ConsultationUiState.Error).message,
                    onRetry = startCallWithPermissions,
                    onBack = { viewModel.resetState() }
                )
            }
            is ConsultationUiState.PostCall -> {
                PostCallScreen(
                    callDuration = viewModel.formatDuration(viewModel.callDuration.collectAsState().value),
                    onDone = { 
                        viewModel.resetState()
                        onBack()
                    }
                )
            }
        }
    }
}

/**
 * Dialog shown when permissions are denied
 */
@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    onRequestAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurface,
        title = {
            Text(
                text = "Permissions Required",
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Camera and Microphone permissions are required for video calls.\n\nPlease grant these permissions to continue.",
                color = TextWhite
            )
        },
        confirmButton = {
            Button(
                onClick = onRequestAgain,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Grant Permissions", color = DeepSpaceBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGrey)
            }
        }
    )
}

/**
 * Pre-consultation screen - Professional patient-facing UI
 */
@Composable
fun PreConsultationScreen(
    doctorInfo: DoctorInfo,
    onStartConsultation: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Text(
            text = "VIDEO CONSULTATION",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Doctor Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Doctor avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .border(3.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍⚕️", style = MaterialTheme.typography.displayMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = doctorInfo.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = doctorInfo.specialty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Availability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NeonLime, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Now",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonLime
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONSULTATION FEATURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow(icon = "📹", title = "HD Video Call", subtitle = "Crystal clear video quality")
                FeatureRow(icon = "🔊", title = "Clear Audio", subtitle = "Studio-quality sound")
                FeatureRow(icon = "🔒", title = "Private & Secure", subtitle = "HIPAA compliant encryption")
                FeatureRow(icon = "📋", title = "Digital Records", subtitle = "Notes saved automatically")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChip(icon = "⏱️", text = "~15 min")
            InfoChip(icon = "💰", text = "Covered")
            InfoChip(icon = "📱", text = "HD Video")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Start Call Button
        Button(
            onClick = onStartConsultation,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scaleOnPress(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepSpaceBlack
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "📹  Start Video Call",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "As easy as a video call",
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureRow(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey
            )
        }
    }
}

@Composable
fun InfoChip(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        }
    }
}

/**
 * Pre-call screen with camera preview
 */
@Composable
fun PreCallScreen(
    viewModel: ConsultationViewModel,
    onStartCall: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isVideoEnabled by viewModel.isLocalVideoEnabled.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isLocalAudioEnabled.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CAMERA PREVIEW",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Camera preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassSurface)
                .border(2.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isVideoEnabled) {
                AgoraLocalVideoView(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = { surface ->
                        viewModel.setupLocalVideo(surface)
                    }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Camera Off", color = TextGrey)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PreCallControlButton(
                icon = if (isAudioEnabled) "🔊" else "🔇",
                label = if (isAudioEnabled) "Mic On" else "Mic Off",
                isActive = isAudioEnabled,
                onClick = { viewModel.toggleMute() }
            )
            
            PreCallControlButton(
                icon = if (isVideoEnabled) "📹" else "📷",
                label = if (isVideoEnabled) "Video On" else "Video Off",
                isActive = isVideoEnabled,
                onClick = { viewModel.toggleCamera() }
            )
            
            PreCallControlButton(
                icon = "🔄",
                label = "Flip",
                onClick = { viewModel.switchCamera() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Join button
        Button(
            onClick = onStartCall,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scaleOnPress(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = DeepSpaceBlack
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "JOIN CALL",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(onClick = onBack) {
            Text("Cancel", color = TextGrey)
        }
    }
}

@Composable
fun PreCallControlButton(
    icon: String,
    label: String,
    isActive: Boolean = true,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (isActive) GlassSurface else NeonRose.copy(alpha = 0.2f),
            modifier = Modifier
                .size(56.dp)
                .scaleOnPress()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextGrey)
    }
}

/**
 * Waiting room - Shows while waiting for other participant
 */
@Composable
fun WaitingRoomScreen(
    doctorInfo: DoctorInfo,
    channelId: String = "",
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated connection indicator
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 4.dp,
                modifier = Modifier.size(100.dp)
            )
            Text("📞", style = MaterialTheme.typography.displayMedium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Connecting...",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Waiting for the other participant to join",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Status card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(NeonLime, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connected to call",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonLime
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusItem(icon = "📹", label = "Video Ready")
                    StatusItem(icon = "🔊", label = "Audio Ready")
                    StatusItem(icon = "📶", label = "Connected")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Cancel button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Cancel Call",
                style = MaterialTheme.typography.titleMedium,
                color = NeonRose
            )
        }
    }
}

@Composable
fun StatusItem(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
    }
}


/**
 * Active video call screen
 */
@Composable
fun VideoCallScreen(
    viewModel: ConsultationViewModel,
    onEndCall: () -> Unit
) {
    val remoteParticipants by viewModel.remoteParticipants.collectAsStateWithLifecycle()
    val isLocalVideoEnabled by viewModel.isLocalVideoEnabled.collectAsStateWithLifecycle()
    val isLocalAudioEnabled by viewModel.isLocalAudioEnabled.collectAsStateWithLifecycle()
    val isVitalsSharing by viewModel.isVitalsSharing.collectAsStateWithLifecycle()
    val currentVitals by viewModel.currentVitals.collectAsStateWithLifecycle()
    val callDuration by viewModel.callDuration.collectAsStateWithLifecycle()
    val networkQuality by viewModel.networkQuality.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize().background(DeepSpaceBlack)) {
        // 1. Remote video (Full Screen Layer)
        if (remoteParticipants.isNotEmpty()) {
            AgoraRemoteVideoView(
                modifier = Modifier.fillMaxSize(),
                uid = remoteParticipants.first().uid,
                onSurfaceReady = { surface, uid ->
                    viewModel.setupRemoteVideo(surface, uid)
                }
            )
        } else {
            // Waiting State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Waiting for doctor...",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextGrey
                    )
                }
            }
        }
        
        // 2. UI Overlay Layer (Gradient for readability)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        // 3. Top Info Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Duration Pill
            Surface(
                color = GlassSurface.copy(alpha = 0.6f),
                shape = CircleShape,
                border = BorderStroke(1.dp, GlassSurface.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NeonRose, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.formatDuration(callDuration),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextWhite
                    )
                }
            }
            
            // Network Quality
            NetworkQualityIndicator(quality = networkQuality)
        }
        
        // 4. Local Video (Picture-in-Picture)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .size(100.dp, 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(2.dp, GlassSurface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            if (isLocalVideoEnabled) {
                AgoraLocalVideoView(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = { surface ->
                        viewModel.setupLocalVideo(surface)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        
        // 5. Vitals Overlay (Left side)
        AnimatedVisibility(
            visible = isVitalsSharing,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            VitalsOverlay(vitals = currentVitals)
        }
        
        // 6. Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic
            ControlFab(
                icon = if (isLocalAudioEnabled) "🎙️" else "🔇",
                isActive = isLocalAudioEnabled,
                onClick = { viewModel.toggleMute() }
            )
            
            // Camera
            ControlFab(
                icon = if (isLocalVideoEnabled) "📹" else "📷",
                isActive = isLocalVideoEnabled,
                onClick = { viewModel.toggleCamera() }
            )
            
            // End Call (Big Red Button)
            FloatingActionButton(
                onClick = onEndCall,
                containerColor = NeonRose,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Text("📞", style = MaterialTheme.typography.headlineMedium)
            }
            
            // Flip
            ControlFab(
                icon = "🔄",
                isActive = true,
                onClick = { viewModel.switchCamera() }
            )
            
            // Vitals
            ControlFab(
                icon = "❤️",
                isActive = isVitalsSharing,
                activeColor = NeonRose,
                onClick = { viewModel.toggleVitalsSharing() }
            )
        }
    }
}

@Composable
fun ControlFab(
    icon: String,
    isActive: Boolean,
    activeColor: Color = GlassSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) activeColor else Color.Black.copy(alpha = 0.6f),
        border = if (!isActive) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null,
        modifier = Modifier.size(56.dp)
            .scaleOnPress()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun NetworkQualityIndicator(quality: Int) {
    Surface(
        color = GlassSurface.copy(alpha = 0.6f),
        shape = CircleShape,
        border = BorderStroke(1.dp, GlassSurface.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📶", 
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Signal bars
            Row(verticalAlignment = Alignment.Bottom) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .width(3.dp)
                            .height(((index + 1) * 3).dp)
                            .background(
                                if (index < (quality.coerceIn(0, 4))) NeonLime else Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun VitalsOverlay(vitals: VitalsPacket) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassSurface.copy(alpha = 0.3f)),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VitalItem(icon = "❤️", value = vitals.heartRate?.toString() ?: "--", unit = "BPM", color = NeonRose)
            VitalItem(icon = "💨", value = vitals.spo2?.toString() ?: "--", unit = "%", color = NeonCyan)
            VitalItem(icon = "🌡️", value = vitals.temperature?.let { String.format("%.1f", it) } ?: "--", unit = "°C", color = NeonLime)
        }
    }
}

@Composable
fun VitalItem(icon: String, value: String, unit: String, color: Color) {
    Column {
        Text(icon, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey.copy(alpha = 0.8f)
        )
    }
}



/**
 * Reconnecting overlay
 */
@Composable
fun ReconnectingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NeonCyan)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reconnecting...",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite
            )
        }
    }
}

/**
 * Error screen
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❌", style = MaterialTheme.typography.displayLarge)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Connection Error",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = NeonRose
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Text("Retry", color = DeepSpaceBlack)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(onClick = onBack) {
            Text("Cancel", color = TextGrey)
        }
    }
}

/**
 * Post-call summary screen - Shows call completion and summary
 */
@Composable
fun PostCallScreen(
    callDuration: String,
    onDone: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Success animation/icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(NeonLime.copy(alpha = 0.2f))
                .border(3.dp, NeonLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✅", style = MaterialTheme.typography.displayMedium)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Consultation Complete",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your video call has ended",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Call Stats Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CALL DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = callDuration,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = TextGrey.copy(alpha = 0.2f))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallStatItem(icon = "📹", label = "Video", value = "HD")
                    CallStatItem(icon = "🔊", label = "Audio", value = "Clear")
                    CallStatItem(icon = "📶", label = "Quality", value = "Good")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Next Steps
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "WHAT'S NEXT?",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(16.dp))
                PostCallItem("📋", "Consultation summary will be available soon")
                PostCallItem("💊", "Prescriptions will appear in your profile")
                PostCallItem("📅", "Schedule follow-up if recommended")
                PostCallItem("⭐", "Rate your experience")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scaleOnPress(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepSpaceBlack
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CallStatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey
        )
    }
}

@Composable
fun PostCallItem(icon: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
    }
}

/**
 * Agora local video view wrapper
 */
@Composable
fun AgoraLocalVideoView(
    modifier: Modifier = Modifier,
    onSurfaceReady: (SurfaceView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).also { surfaceView ->
                onSurfaceReady(surfaceView)
            }
        }
    )
}

/**
 * Agora remote video view wrapper
 */
@Composable
fun AgoraRemoteVideoView(
    modifier: Modifier = Modifier,
    uid: Int,
    onSurfaceReady: (SurfaceView, Int) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).also { surfaceView ->
                onSurfaceReady(surfaceView, uid)
            }
        }
    )
}

/**
 * Screen shown when Agora App ID is not configured
 */
@Composable
fun NotConfiguredScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚙️", style = MaterialTheme.typography.displayLarge)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Setup Required",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = NeonCyan
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Video calling needs Agora SDK configuration",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QUICK SETUP (2 minutes)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonLime
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                SetupStep("1️⃣", "Go to console.agora.io")
                SetupStep("2️⃣", "Create FREE account")
                SetupStep("3️⃣", "Create a new project")
                SetupStep("4️⃣", "Select \"Testing Mode\"")
                SetupStep("5️⃣", "Copy App ID")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Paste App ID in:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
                Text(
                    text = "AgoraConfig.kt → APP_ID",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "💡 Agora offers 10,000 FREE minutes/month!",
            style = MaterialTheme.typography.labelMedium,
            color = NeonLime,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = onBack) {
            Text("← Go Back", color = TextGrey)
        }
    }
}

@Composable
fun SetupStep(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
    }
}
