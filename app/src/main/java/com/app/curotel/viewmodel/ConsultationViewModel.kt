package com.app.curotel.viewmodel

import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.curotel.data.repository.DeviceRepository
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.service.agora.AgoraConfig
import com.app.curotel.service.agora.AgoraVideoService
import com.app.curotel.service.agora.CallState
import com.app.curotel.service.agora.RemoteParticipant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Consultation state for UI
 */
sealed class ConsultationUiState {
    object Idle : ConsultationUiState()
    object NotConfigured : ConsultationUiState() // App ID not set
    object PreCall : ConsultationUiState()
    object WaitingForDoctor : ConsultationUiState()
    object InCall : ConsultationUiState()
    object Reconnecting : ConsultationUiState()
    data class Error(val message: String) : ConsultationUiState()
    object PostCall : ConsultationUiState()
}

/**
 * Doctor information
 */
data class DoctorInfo(
    val id: String = "",
    val name: String = "Dr. Sarah Johnson",
    val specialty: String = "General Physician",
    val avatarUrl: String? = null,
    val isAvailable: Boolean = true
)

/**
 * ViewModel for video consultation feature
 * Works standalone - NO backend required!
 * Just needs Agora App ID from console.agora.io
 */
@HiltViewModel
class ConsultationViewModel @Inject constructor(
    private val agoraService: AgoraVideoService,
    private val agoraChatService: com.app.curotel.service.agora.AgoraChatService,
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    init {
        agoraChatService.initialize()
    }
    
    // ========== Role State ==========
    private val _isDoctorMode = MutableStateFlow(false)
    val isDoctorMode: StateFlow<Boolean> = _isDoctorMode.asStateFlow()

    fun toggleRole() {
        _isDoctorMode.value = !_isDoctorMode.value
        // Logout of chat so we can re-login with new role
        agoraChatService.logout()
    }

    // ========== Agora State ==========
    
    val callState: StateFlow<CallState> = agoraService.callState
    val remoteParticipants: StateFlow<List<RemoteParticipant>> = agoraService.remoteParticipants
    val isLocalVideoEnabled: StateFlow<Boolean> = agoraService.isLocalVideoEnabled
    val isLocalAudioEnabled: StateFlow<Boolean> = agoraService.isLocalAudioEnabled
    val networkQuality: StateFlow<Int> = agoraService.networkQuality
    
    // ========== UI State ==========
    
    private val _uiState = MutableStateFlow<ConsultationUiState>(ConsultationUiState.Idle)
    val uiState: StateFlow<ConsultationUiState> = _uiState.asStateFlow()
    
    private val _doctorInfo = MutableStateFlow(DoctorInfo())
    val doctorInfo: StateFlow<DoctorInfo> = _doctorInfo.asStateFlow()
    
    private val _isVitalsSharing = MutableStateFlow(false)
    val isVitalsSharing: StateFlow<Boolean> = _isVitalsSharing.asStateFlow()
    
    private val _callDuration = MutableStateFlow(0) // seconds
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()
    
    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()
    
    private val _currentChannelId = MutableStateFlow("")
    val currentChannelId: StateFlow<String> = _currentChannelId.asStateFlow()
    
    // ========== Chat State ==========
    val chatMessages = agoraChatService.messages
    
    // Controls if the Chat Screen acts as a full separate screen overlay
    private val _showChatScreen = MutableStateFlow(false)
    val showChatScreen: StateFlow<Boolean> = _showChatScreen.asStateFlow()
    
    // Kept for backward compatibility if needed, but primarily using showChatScreen now
    private val _isChatVisible = MutableStateFlow(false)
    val isChatVisible: StateFlow<Boolean> = _isChatVisible.asStateFlow()
    
    // Vitals from device
    val currentVitals: StateFlow<VitalsPacket> = deviceRepository.currentVitals
    
    // ========== Error handling ==========
    
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    
    init {
        // Don't initialize Agora here - it will be initialized when joining channel
        // This avoids context timing issues with Hilt injection
        if (!AgoraConfig.isConfigured()) {
            _uiState.value = ConsultationUiState.NotConfigured
        }
        
        // Observe call state changes
        viewModelScope.launch {
            callState.collect { state ->
                when (state) {
                    is CallState.Connected -> {
                        _uiState.value = if (remoteParticipants.value.isEmpty()) {
                            ConsultationUiState.WaitingForDoctor
                        } else {
                            ConsultationUiState.InCall
                        }
                        startCallTimer()
                    }
                    is CallState.Reconnecting -> {
                        _uiState.value = ConsultationUiState.Reconnecting
                    }
                    is CallState.Error -> {
                        _uiState.value = ConsultationUiState.Error(state.message)
                        _errorMessage.emit(state.message)
                    }
                    is CallState.Ended -> {
                        _uiState.value = ConsultationUiState.PostCall
                    }
                    else -> {}
                }
            }
        }
        
        // Watch for remote participants joining/leaving
        viewModelScope.launch {
            remoteParticipants.collect { participants ->
                val currentState = callState.value
                if (currentState is CallState.Connected) {
                    when {
                        participants.isNotEmpty() && _uiState.value == ConsultationUiState.WaitingForDoctor -> {
                            // Remote user joined - start the call!
                            _uiState.value = ConsultationUiState.InCall
                        }
                        participants.isEmpty() && _uiState.value == ConsultationUiState.InCall -> {
                            // Remote user left the call - show post call screen
                            _uiState.value = ConsultationUiState.PostCall
                            agoraService.leaveChannel()
                        }
                    }
                }
            }
        }
    }
    
    // ========== Consultation Actions ==========
    
    /**
     * Start pre-call setup (camera preview)
     */
    fun startPreCall() {
        if (!AgoraConfig.isConfigured()) {
            _uiState.value = ConsultationUiState.NotConfigured
            return
        }
        _uiState.value = ConsultationUiState.PreCall
    }
    
    /**
     * Start a consultation - NO backend required!
     * Uses a fixed demo channel so both participants can join
     */
    fun startConsultation() {
        if (!AgoraConfig.isConfigured()) {
            viewModelScope.launch {
                _errorMessage.emit("Please set Agora App ID in AgoraConfig.kt")
            }
            _uiState.value = ConsultationUiState.NotConfigured
            return
        }
        
        viewModelScope.launch {
            try {
                // Use fixed demo channel - both patient and doctor join this
                val channelId = AgoraConfig.DEMO_CHANNEL
                _currentChannelId.value = channelId
                
                // PRIORITY 1: USE TEMP TOKEN FROM CONFIG IF SET
                // PRIORITY 2: GENERATE LOCAL TOKEN IF CERTIFICATE PRESENT
                // PRIORITY 3: NULL (TESTING MODE ONLY)
                val token = if (AgoraConfig.TEMP_TOKEN?.isNotBlank() == true) {
                    AgoraConfig.TEMP_TOKEN
                } else if (AgoraConfig.APP_CERTIFICATE?.isNotEmpty() == true) {
                    com.app.curotel.service.agora.AgoraTokenGenerator.generateToken(
                        AgoraConfig.APP_ID,
                        AgoraConfig.APP_CERTIFICATE!!,
                        channelId,
                        0, // uid
                        1, // role publisher
                        (System.currentTimeMillis() / 1000 + 3600).toInt() // 1 hour expiry
                    )
                } else null

                agoraService.joinChannel(
                    token = token,
                    channelId = channelId,
                    uid = 0 // Auto-assign UID
                )
                
                // Login to Chat with configured credentials based on selected role
                val isDoctor = _isDoctorMode.value
                val chatUser = if (isDoctor) AgoraConfig.DEMO_USER_DOCTOR else AgoraConfig.DEMO_USER_PATIENT
                val chatToken = if (isDoctor) AgoraConfig.CHAT_TOKEN_DOCTOR else AgoraConfig.CHAT_TOKEN_PATIENT
                
                agoraChatService.login(chatUser, chatToken ?: "", 
                    onSuccess = { 
                        android.util.Log.d("ConsultationVM", "Chat login success as $chatUser")
                    },
                    onError = { code, msg ->
                        android.util.Log.e("ConsultationVM", "Chat login failed: $code - $msg")
                    }
                )
                
                _uiState.value = ConsultationUiState.WaitingForDoctor
                
            } catch (e: Exception) {
                _errorMessage.emit("Failed to start consultation: ${e.message}")
                _uiState.value = ConsultationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Join a specific channel by ID
     * Useful for testing - both devices can join the same channel
     */
    fun joinChannel(channelId: String) {
        if (!AgoraConfig.isConfigured()) {
            viewModelScope.launch {
                _errorMessage.emit("Please set Agora App ID in AgoraConfig.kt")
            }
            return
        }
        
        viewModelScope.launch {
            try {
                _currentChannelId.value = channelId
                
                // PRIORITY 1: USE TEMP TOKEN FROM CONFIG IF SET
                val token = if (AgoraConfig.TEMP_TOKEN?.isNotBlank() == true) {
                    AgoraConfig.TEMP_TOKEN
                } else if (AgoraConfig.APP_CERTIFICATE?.isNotEmpty() == true) {
                    com.app.curotel.service.agora.AgoraTokenGenerator.generateToken(
                        AgoraConfig.APP_ID,
                        AgoraConfig.APP_CERTIFICATE!!,
                        channelId,
                        0,
                        1,
                        (System.currentTimeMillis() / 1000 + 3600).toInt()
                    )
                } else null
                
                agoraService.joinChannel(
                    token = token,
                    channelId = channelId,
                    uid = 0
                )
                
                // Login to Chat with configured credentials
                val isDoctor = _isDoctorMode.value
                val chatUser = if (isDoctor) AgoraConfig.DEMO_USER_DOCTOR else AgoraConfig.DEMO_USER_PATIENT
                val chatToken = if (isDoctor) AgoraConfig.CHAT_TOKEN_DOCTOR else AgoraConfig.CHAT_TOKEN_PATIENT
                
                agoraChatService.login(chatUser, chatToken ?: "", 
                    onSuccess = { android.util.Log.d("ConsultationVM", "Chat login success") },
                    onError = { code, msg -> android.util.Log.e("ConsultationVM", "Chat error: $code") }
                )
                
                _uiState.value = ConsultationUiState.WaitingForDoctor
                
            } catch (e: Exception) {
                _errorMessage.emit("Failed to join channel: ${e.message}")
                _uiState.value = ConsultationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * End the current consultation
     */
    fun endConsultation() {
        agoraService.leaveChannel()
        _uiState.value = ConsultationUiState.PostCall
        _callDuration.value = 0
    }
    
    /**
     * Return to idle state
     */
    fun resetState() {
        _uiState.value = if (AgoraConfig.isConfigured()) {
            ConsultationUiState.Idle
        } else {
            ConsultationUiState.NotConfigured
        }
        _callDuration.value = 0
        _isVitalsSharing.value = false
    }
    
    // ========== Video Controls ==========
    
    /**
     * Setup local video preview
     */
    fun setupLocalVideo(surfaceView: SurfaceView) {
        agoraService.setupLocalVideo(surfaceView)
    }
    
    /**
     * Setup remote video display
     */
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        agoraService.setupRemoteVideo(surfaceView, uid)
    }
    
    /**
     * Toggle local audio (mute/unmute)
     */
    fun toggleMute() {
        agoraService.toggleLocalAudio()
    }
    
    /**
     * Toggle local video (on/off)
     */
    fun toggleCamera() {
        agoraService.toggleLocalVideo()
    }
    
    /**
     * Switch between front and back camera
     */
    fun switchCamera() {
        agoraService.switchCamera()
    }
    
    /**
     * Toggle speaker/earpiece
     */
    fun toggleSpeaker() {
        val newState = !_isSpeakerOn.value
        agoraService.enableSpeakerphone(newState)
        _isSpeakerOn.value = newState
    }
    
    /**
     * Toggle vitals sharing overlay
     */
    fun toggleVitalsSharing() {
        _isVitalsSharing.value = !_isVitalsSharing.value
    }

    // ========== Chat Actions ==========

    fun toggleChat() {
        // Toggle the full screen chat state
        _showChatScreen.value = !_showChatScreen.value
        // Also toggle the old overlay state just in case
        _isChatVisible.value = !_isChatVisible.value
    }
    
    fun navigateToChat() {
        _showChatScreen.value = true
    }
    
    fun navigateToVideoCall() {
        _showChatScreen.value = false
    }

    fun sendChatMessage(content: String) {
        if (!AgoraConfig.isChatConfigured()) {
            android.util.Log.e("ConsultationVM", "Chat not configured")
            return
        }
        
        // Target the *other* user
        val isDoctor = _isDoctorMode.value
        val targetUser = if (isDoctor) AgoraConfig.DEMO_USER_PATIENT else AgoraConfig.DEMO_USER_DOCTOR
        
        agoraChatService.sendMessage(content, targetUser)
    }
    
    // ========== Timer ==========
    
    private fun startCallTimer() {
        viewModelScope.launch {
            while (callState.value is CallState.Connected) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }
    
    /**
     * Format call duration as MM:SS
     */
    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    // ========== Cleanup ==========
    


    /**
     * Ensure we are logged into Chat
     * Called when ChatScreen is opened directly
     */
    fun ensureChatLogin() {
        val isDoctor = _isDoctorMode.value
        val expectedUser = if (isDoctor) AgoraConfig.DEMO_USER_DOCTOR else AgoraConfig.DEMO_USER_PATIENT
        val expectedToken = if (isDoctor) AgoraConfig.CHAT_TOKEN_DOCTOR else AgoraConfig.CHAT_TOKEN_PATIENT

        if (agoraChatService.isLoggedIn()) {
            val currentUser = agoraChatService.getCurrentUser()
            if (currentUser == expectedUser) {
                android.util.Log.d("ConsultationVM", "Already logged in as correct user: $currentUser")
                return
            } else {
                android.util.Log.w("ConsultationVM", "Logged in as wrong user ($currentUser), expected $expectedUser. Relogging...")
                agoraChatService.logout()
            }
        }
        
        if (!AgoraConfig.isChatConfigured()) return
        
        android.util.Log.d("ConsultationVM", "Auto-logging in to chat as $expectedUser")
        
        agoraChatService.login(expectedUser, expectedToken ?: "", 
            onSuccess = { 
                android.util.Log.d("ConsultationVM", "Chat auto-login success")
                // Refresh messages or notify UI if needed
            },
            onError = { code, msg ->
                android.util.Log.e("ConsultationVM", "Chat auto-login failed: $code - $msg")
            }
        )
    }

    override fun onCleared() {
        agoraService.destroy()
        // Don't destroy ChatService as it is Singleton and might be reused
        // agoraChatService.onDestroy() 
        super.onCleared()
    }
}
