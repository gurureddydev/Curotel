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
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
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
                
                // Join the channel with null token (Testing Mode in Agora Console)
                agoraService.joinChannel(
                    token = null, // No token needed in Testing Mode
                    channelId = channelId,
                    uid = 0 // Auto-assign UID
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
                
                agoraService.joinChannel(
                    token = null,
                    channelId = channelId,
                    uid = 0
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
    
    override fun onCleared() {
        agoraService.destroy()
        super.onCleared()
    }
}
