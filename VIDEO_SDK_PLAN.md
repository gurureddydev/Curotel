# 📹 Video SDK Implementation Plan

## Curotel Telemedicine - Video Consultation Feature

This document outlines the complete plan for implementing video consultation using Agora SDK (recommended) or WebRTC.

---

## 📊 Overview

| Aspect | Details |
|--------|---------|
| **Feature** | Real-time video consultation between patient and doctor |
| **SDK Choice** | Agora.io (recommended) or WebRTC |
| **Timeline** | 2-3 days |
| **Complexity** | High |
| **Dependencies** | Backend for token generation |

---

## 🎯 Why Agora over WebRTC?

| Feature | Agora | WebRTC (Raw) |
|---------|-------|--------------|
| Setup complexity | Low (SDK handles all) | High (manual STUN/TURN) |
| Reliability | 99.9% SLA | Depends on implementation |
| Scaling | Built-in global network | Need to build infrastructure |
| Features | Video, Audio, Screen share, Recording | Manual implementation |
| Documentation | Excellent | Scattered |
| Cost | Pay per minute (~$0.99/1000 min) | Free but infra cost |

**Recommendation:** Use **Agora** for faster implementation and reliability.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Curotel App (Patient)                   │
├─────────────────────────────────────────────────────────────┤
│  ConsultScreen.kt                                           │
│  ├── VideoCallScreen (active call UI)                       │
│  ├── PreCallScreen (camera preview, settings)              │
│  ├── WaitingRoom (waiting for doctor)                       │
│  └── PostCallScreen (feedback, summary)                     │
├─────────────────────────────────────────────────────────────┤
│  AgoraVideoService                                          │
│  ├── initializeEngine()                                     │
│  ├── joinChannel(token, channelId, userId)                 │
│  ├── leaveChannel()                                         │
│  ├── toggleMute()                                           │
│  ├── toggleCamera()                                         │
│  ├── switchCamera()                                         │
│  └── shareVitals()                                          │
├─────────────────────────────────────────────────────────────┤
│  Agora SDK                                                  │
│  ├── RtcEngine                                              │
│  ├── VideoCanvas                                            │
│  └── IRtcEngineEventHandler                                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ WebSocket / REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Backend Server                          │
├─────────────────────────────────────────────────────────────┤
│  /api/v1/consultation/start                                 │
│  ├── Generate Agora token                                   │
│  ├── Create channel                                         │
│  └── Return { token, channelId, uid }                       │
├─────────────────────────────────────────────────────────────┤
│  /api/v1/consultation/end                                   │
│  ├── End session                                            │
│  ├── Save recording URL                                     │
│  └── Generate summary                                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Agora Cloud Services                      │
├─────────────────────────────────────────────────────────────┤
│  ├── Real-time video/audio                                  │
│  ├── Cloud recording (optional)                             │
│  ├── Analytics                                              │
│  └── Global edge network                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📅 Implementation Timeline

### Day 1: Setup & Basic Integration (8 hours)

| Time | Task | Details |
|------|------|---------|
| 1h | Agora account setup | Create account, get App ID & Certificate |
| 1h | Add SDK dependencies | Gradle configuration |
| 2h | Create AgoraVideoService | Engine initialization, event handling |
| 2h | Create PreCallScreen | Camera preview, permission handling |
| 2h | Basic VideoCallScreen | Join channel, display local/remote video |

### Day 2: Full Call Features (8 hours)

| Time | Task | Details |
|------|------|---------|
| 2h | Call controls | Mute, camera toggle, speaker, switch camera |
| 2h | Connection states | Connecting, connected, reconnecting, failed |
| 2h | Vitals sharing overlay | Share live readings during call |
| 2h | Backend token integration | API for token generation |

### Day 3: Polish & Edge Cases (8 hours)

| Time | Task | Details |
|------|------|---------|
| 2h | WaitingRoom | Queue system, doctor availability |
| 2h | PostCallScreen | Feedback, call summary, next steps |
| 2h | Error handling | Network issues, permission denied, etc. |
| 2h | Testing & debugging | Various scenarios, devices |

---

## 📁 File Structure

```
app/src/main/java/com/app/curotel/
├── data/
│   └── remote/
│       └── api/
│           └── ConsultationApiService.kt    # API for consultation
│
├── di/
│   └── VideoModule.kt                        # Hilt module for Agora
│
├── domain/
│   └── model/
│       └── ConsultationModels.kt            # Data models
│
├── service/
│   └── agora/
│       ├── AgoraVideoService.kt             # Core Agora service
│       ├── AgoraEventHandler.kt             # Event callbacks
│       └── AgoraConfig.kt                   # Configuration
│
├── ui/
│   └── consultation/
│       ├── ConsultationScreen.kt            # Main screen container
│       ├── PreCallScreen.kt                 # Pre-call setup
│       ├── VideoCallScreen.kt               # Active call UI
│       ├── WaitingRoomScreen.kt             # Waiting for doctor
│       ├── PostCallScreen.kt                # After call summary
│       └── components/
│           ├── VideoSurface.kt              # Agora video view
│           ├── CallControls.kt              # Mute, camera buttons
│           ├── VitalsOverlay.kt             # Share vitals during call
│           └── ParticipantInfo.kt           # Doctor/patient info
│
└── viewmodel/
    └── ConsultationViewModel.kt             # Call state management
```

---

## 🔧 Implementation Details

### Step 1: Add Agora SDK

**gradle/libs.versions.toml**
```toml
[versions]
agora = "4.2.6"

[libraries]
agora-rtc = { group = "io.agora.rtc", name = "full-sdk", version.ref = "agora" }
```

**app/build.gradle.kts**
```kotlin
dependencies {
    implementation(libs.agora.rtc)
}
```

**AndroidManifest.xml** (already have most)
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
```

---

### Step 2: Agora Configuration

**AgoraConfig.kt**
```kotlin
object AgoraConfig {
    // Get from Agora Console (https://console.agora.io)
    const val APP_ID = "YOUR_AGORA_APP_ID"
    
    // Token server URL (your backend)
    const val TOKEN_SERVER_URL = "https://api.curotel.com/api/v1/agora/token"
    
    // Video settings
    object Video {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val FRAME_RATE = 15
        const val BITRATE = 400
    }
}
```

---

### Step 3: Agora Video Service

**AgoraVideoService.kt**
```kotlin
@Singleton
class AgoraVideoService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rtcEngine: RtcEngine? = null
    
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    private val _remoteUsers = MutableStateFlow<List<Int>>(emptyList())
    val remoteUsers: StateFlow<List<Int>> = _remoteUsers.asStateFlow()
    
    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            _remoteUsers.value = _remoteUsers.value + uid
        }
        
        override fun onUserOffline(uid: Int, reason: Int) {
            _remoteUsers.value = _remoteUsers.value - uid
        }
        
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            _callState.value = CallState.Connected
        }
        
        override fun onConnectionLost() {
            _callState.value = CallState.Reconnecting
        }
        
        override fun onError(err: Int) {
            _callState.value = CallState.Error(err)
        }
    }
    
    fun initialize(): Boolean {
        return try {
            val config = RtcEngineConfig().apply {
                mContext = context
                mAppId = AgoraConfig.APP_ID
                mEventHandler = eventHandler
            }
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableVideo()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun joinChannel(token: String, channelId: String, uid: Int) {
        _callState.value = CallState.Connecting
        
        val options = ChannelMediaOptions().apply {
            autoSubscribeAudio = true
            autoSubscribeVideo = true
            publishMicrophoneTrack = true
            publishCameraTrack = true
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        
        rtcEngine?.joinChannel(token, channelId, uid, options)
    }
    
    fun leaveChannel() {
        rtcEngine?.leaveChannel()
        _callState.value = CallState.Idle
        _remoteUsers.value = emptyList()
    }
    
    fun toggleMute(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
    }
    
    fun toggleCamera(enabled: Boolean) {
        rtcEngine?.enableLocalVideo(enabled)
    }
    
    fun switchCamera() {
        rtcEngine?.switchCamera()
    }
    
    fun setupLocalVideo(surfaceView: SurfaceView) {
        val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
        rtcEngine?.setupLocalVideo(canvas)
        rtcEngine?.startPreview()
    }
    
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        rtcEngine?.setupRemoteVideo(canvas)
    }
    
    fun destroy() {
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }
}

sealed class CallState {
    object Idle : CallState()
    object Connecting : CallState()
    object Connected : CallState()
    object Reconnecting : CallState()
    data class Error(val code: Int) : CallState()
    object Ended : CallState()
}
```

---

### Step 4: Consultation ViewModel

**ConsultationViewModel.kt**
```kotlin
@HiltViewModel
class ConsultationViewModel @Inject constructor(
    private val agoraService: AgoraVideoService,
    private val apiService: CurotelApiService,
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    val callState = agoraService.callState
    val remoteUsers = agoraService.remoteUsers
    val currentVitals = deviceRepository.currentVitals
    
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    
    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()
    
    private val _isVitalsSharing = MutableStateFlow(false)
    val isVitalsSharing: StateFlow<Boolean> = _isVitalsSharing.asStateFlow()
    
    init {
        agoraService.initialize()
    }
    
    fun startConsultation(doctorId: String) {
        viewModelScope.launch {
            try {
                // Get token from backend
                val response = apiService.startConsultation(
                    mapOf("doctor_id" to doctorId)
                )
                
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    data?.let {
                        agoraService.joinChannel(
                            token = it.videoToken ?: "",
                            channelId = it.id,
                            uid = 0 // Auto-assign
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun endConsultation() {
        viewModelScope.launch {
            agoraService.leaveChannel()
        }
    }
    
    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        agoraService.toggleMute(_isMuted.value)
    }
    
    fun toggleCamera() {
        _isCameraOn.value = !_isCameraOn.value
        agoraService.toggleCamera(_isCameraOn.value)
    }
    
    fun switchCamera() {
        agoraService.switchCamera()
    }
    
    fun toggleVitalsSharing() {
        _isVitalsSharing.value = !_isVitalsSharing.value
    }
    
    fun setupLocalVideo(surfaceView: SurfaceView) {
        agoraService.setupLocalVideo(surfaceView)
    }
    
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        agoraService.setupRemoteVideo(surfaceView, uid)
    }
    
    override fun onCleared() {
        agoraService.destroy()
        super.onCleared()
    }
}
```

---

### Step 5: Video Call Screen

**VideoCallScreen.kt**
```kotlin
@Composable
fun VideoCallScreen(
    viewModel: ConsultationViewModel = hiltViewModel(),
    onCallEnd: () -> Unit
) {
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val remoteUsers by viewModel.remoteUsers.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isCameraOn by viewModel.isCameraOn.collectAsStateWithLifecycle()
    val isVitalsSharing by viewModel.isVitalsSharing.collectAsStateWithLifecycle()
    val currentVitals by viewModel.currentVitals.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Remote video (doctor) - full screen
        if (remoteUsers.isNotEmpty()) {
            AgoraVideoSurface(
                modifier = Modifier.fillMaxSize(),
                onSurfaceReady = { surface ->
                    viewModel.setupRemoteVideo(surface, remoteUsers.first())
                }
            )
        } else {
            // Waiting for doctor
            Box(
                modifier = Modifier.fillMaxSize().background(DeepSpaceBlack),
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
            is CallState.Error -> ConnectionBanner("Connection error")
            else -> {}
        }
        
        // Call controls
        CallControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            isMuted = isMuted,
            isCameraOn = isCameraOn,
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
            label = if (isMuted) "Unmute" else "Mute",
            isActive = !isMuted,
            onClick = onMuteToggle
        )
        
        // Camera toggle
        CallControlButton(
            icon = if (isCameraOn) "📷" else "📷",
            label = if (isCameraOn) "Camera On" else "Camera Off",
            isActive = isCameraOn,
            onClick = onCameraToggle
        )
        
        // Switch camera
        CallControlButton(
            icon = "🔄",
            label = "Switch",
            onClick = onSwitchCamera
        )
        
        // Share vitals
        CallControlButton(
            icon = "❤️",
            label = "Vitals",
            isActive = isVitalsSharing,
            onClick = onVitalsToggle
        )
        
        // End call
        CallControlButton(
            icon = "📵",
            label = "End",
            color = NeonRose,
            onClick = onEndCall
        )
    }
}

@Composable
fun VitalsOverlay(
    vitals: VitalsPacket,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
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
```

---

## 🔐 Backend Requirements

Your backend needs to implement token generation:

**POST /api/v1/agora/token**
```json
// Request
{
  "channel_id": "consultation_123",
  "uid": 12345,
  "role": "publisher"
}

// Response
{
  "token": "006xxxxxxxxxxxxxxx",
  "expires_at": 1699999999
}
```

**Token Generation (Node.js example)**
```javascript
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

function generateToken(channelName, uid, role) {
  const appId = process.env.AGORA_APP_ID;
  const appCertificate = process.env.AGORA_APP_CERTIFICATE;
  const expirationTimeInSeconds = 3600; // 1 hour
  
  const currentTimestamp = Math.floor(Date.now() / 1000);
  const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;
  
  return RtcTokenBuilder.buildTokenWithUid(
    appId,
    appCertificate,
    channelName,
    uid,
    role === 'publisher' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER,
    privilegeExpiredTs
  );
}
```

---

## ✅ Testing Checklist

| Scenario | Test |
|----------|------|
| Happy path | Patient joins, doctor joins, call works |
| Permission denied | Handle camera/mic permission rejection |
| Network loss | Reconnection handling |
| Doctor late | Waiting room experience |
| Call end | Proper cleanup, feedback screen |
| Switch camera | Front/back camera switching |
| Mute/unmute | Audio control verification |
| Vitals sharing | Live data visible to doctor |
| Background | App behavior when backgrounded |
| Low bandwidth | Quality degradation handling |

---

## 💰 Agora Pricing

| Usage | Free Tier | Paid |
|-------|-----------|------|
| Video | 10,000 min/month | $0.99/1000 min |
| Audio | 10,000 min/month | $0.49/1000 min |
| Recording | Not included | $1.49/1000 min |

For a medical app, expect ~15-30 min per consultation:
- **1000 consultations/month** ≈ $15-30/month

---

## 🚀 Quick Start Commands

```bash
# 1. Create Agora account
open https://console.agora.io

# 2. Add SDK (already in version catalog)
./gradlew build

# 3. Test with Agora sample app
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

*Ready to implement? Let me know and I'll start coding!*

---

*Document Version: 1.0 | Created: 2026-01-20*
