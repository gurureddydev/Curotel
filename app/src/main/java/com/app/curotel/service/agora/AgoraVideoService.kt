package com.app.curotel.service.agora

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AgoraVideoService"

/**
 * Sealed class representing the current call state
 */
sealed class CallState {
    object Idle : CallState()
    object Initializing : CallState()
    object Connecting : CallState()
    object Connected : CallState()
    object Reconnecting : CallState()
    data class Error(val code: Int, val message: String) : CallState()
    object Ended : CallState()
}

/**
 * Data class for remote participant info
 */
data class RemoteParticipant(
    val uid: Int,
    val isVideoEnabled: Boolean = true,
    val isAudioEnabled: Boolean = true
)

/**
 * Core Agora Video Service for telemedicine consultations
 * Handles all RTC engine operations, video/audio streaming
 */
@Singleton
class AgoraVideoService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false
    private var currentChannelId: String? = null
    private var localUid: Int = 0
    
    // Call state
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    // Remote participants
    private val _remoteParticipants = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    val remoteParticipants: StateFlow<List<RemoteParticipant>> = _remoteParticipants.asStateFlow()
    
    // Local video/audio state
    private val _isLocalVideoEnabled = MutableStateFlow(true)
    val isLocalVideoEnabled: StateFlow<Boolean> = _isLocalVideoEnabled.asStateFlow()
    
    private val _isLocalAudioEnabled = MutableStateFlow(true)
    val isLocalAudioEnabled: StateFlow<Boolean> = _isLocalAudioEnabled.asStateFlow()
    
    // Network quality
    private val _networkQuality = MutableStateFlow(0) // 0-5, 0 = unknown
    val networkQuality: StateFlow<Int> = _networkQuality.asStateFlow()
    
    /**
     * RTC Engine event handler
     */
    private val eventHandler = object : IRtcEngineEventHandler() {
        
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "✅ onJoinChannelSuccess: channel=$channel, uid=$uid, elapsed=$elapsed ms")
            localUid = uid
            _callState.value = CallState.Connected
        }
        
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "✅ onUserJoined: uid=$uid - REMOTE USER CONNECTED!")
            val participant = RemoteParticipant(uid = uid)
            _remoteParticipants.value = _remoteParticipants.value + participant
        }
        
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "👋 onUserOffline: uid=$uid, reason=$reason")
            _remoteParticipants.value = _remoteParticipants.value.filter { it.uid != uid }
        }
        
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "📹 onRemoteVideoStateChanged: uid=$uid, state=$state")
            _remoteParticipants.value = _remoteParticipants.value.map {
                if (it.uid == uid) it.copy(isVideoEnabled = state == 2) else it
            }
        }
        
        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "🔊 onRemoteAudioStateChanged: uid=$uid, state=$state")
            _remoteParticipants.value = _remoteParticipants.value.map {
                if (it.uid == uid) it.copy(isAudioEnabled = state == 2) else it
            }
        }
        
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.d(TAG, "🔗 onConnectionStateChanged: state=$state, reason=$reason")
            when (state) {
                Constants.CONNECTION_STATE_CONNECTING -> _callState.value = CallState.Connecting
                Constants.CONNECTION_STATE_CONNECTED -> _callState.value = CallState.Connected
                Constants.CONNECTION_STATE_RECONNECTING -> _callState.value = CallState.Reconnecting
                Constants.CONNECTION_STATE_FAILED -> _callState.value = CallState.Error(reason, "Connection failed")
            }
        }
        
        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            if (uid == 0 || uid == localUid) {
                _networkQuality.value = minOf(txQuality, rxQuality)
            }
        }
        
        override fun onError(err: Int) {
            Log.e(TAG, "❌ onError: code=$err")
            val message = when (err) {
                ErrorCode.ERR_INVALID_TOKEN -> "Invalid token"
                ErrorCode.ERR_TOKEN_EXPIRED -> "Token expired"
                ErrorCode.ERR_INVALID_APP_ID -> "Invalid App ID"
                else -> "Error: $err"
            }
            _callState.value = CallState.Error(err, message)
        }
        
        override fun onLeaveChannel(stats: RtcStats?) {
            Log.d(TAG, "📴 onLeaveChannel")
            _callState.value = CallState.Ended
            _remoteParticipants.value = emptyList()
        }
    }
    
    /**
     * Initialize the RTC Engine
     */
    fun initialize(): Boolean {
        if (isInitialized && rtcEngine != null) {
            Log.d(TAG, "✅ RTC Engine already initialized")
            return true
        }
        
        Log.d(TAG, "🚀 Initializing Agora RTC Engine...")
        Log.d(TAG, "   App ID: ${AgoraConfig.APP_ID.take(10)}...")
        
        _callState.value = CallState.Initializing
        
        return try {
            val appContext = context.applicationContext ?: context
            Log.d(TAG, "   Context: $appContext")
            
            val config = RtcEngineConfig().apply {
                mContext = appContext
                mAppId = AgoraConfig.APP_ID
                mEventHandler = eventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            }
            
            rtcEngine = RtcEngine.create(config)
            
            if (rtcEngine == null) {
                Log.e(TAG, "❌ RtcEngine.create() returned null!")
                _callState.value = CallState.Error(-1, "Failed to create RTC Engine")
                return false
            }
            
            Log.d(TAG, "✅ RTC Engine created")
            
            rtcEngine?.apply {
                setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                enableAudio()
                enableVideo()
                setVideoEncoderConfiguration(
                    VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VideoDimensions(640, 480),
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        400,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    )
                )
                startPreview()
            }
            
            Log.d(TAG, "✅ Audio/Video enabled, Preview started")
            
            isInitialized = true
            _callState.value = CallState.Idle
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize: ${e.message}", e)
            _callState.value = CallState.Error(-1, "Init failed: ${e.message}")
            false
        }
    }
    
    /**
     * Join a video call channel
     */
    fun joinChannel(token: String?, channelId: String, uid: Int = 0) {
        Log.d(TAG, "📞 Joining channel: $channelId")
        
        if (!isInitialized || rtcEngine == null) {
            Log.d(TAG, "   Initializing first...")
            if (!initialize()) {
                Log.e(TAG, "   ❌ Failed to initialize")
                return
            }
        }
        
        _callState.value = CallState.Connecting
        currentChannelId = channelId
        
        val options = ChannelMediaOptions().apply {
            autoSubscribeAudio = true
            autoSubscribeVideo = true
            publishMicrophoneTrack = true
            publishCameraTrack = true
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        
        val effectiveToken = token ?: AgoraConfig.TEMP_TOKEN ?: ""
        Log.d(TAG, "   Token: ${if (effectiveToken.isNotEmpty()) "yes" else "no"}")
        
        val result = rtcEngine?.joinChannel(effectiveToken, channelId, uid, options) ?: -1
        Log.d(TAG, "   ✅ joinChannel result: $result (0=success)")
        
        if (result != 0) {
            _callState.value = CallState.Error(result, "Join failed: $result")
        }
    }
    
    fun leaveChannel() {
        Log.d(TAG, "📴 Leaving channel")
        rtcEngine?.leaveChannel()
        currentChannelId = null
        _callState.value = CallState.Idle
        _remoteParticipants.value = emptyList()
    }
    
    fun setupLocalVideo(surfaceView: SurfaceView) {
        Log.d(TAG, "📹 Setup local video")
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }
    
    fun setupLocalVideoTexture(textureView: TextureView) {
        rtcEngine?.setupLocalVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }
    
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        Log.d(TAG, "📹 Setup remote video: uid=$uid")
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }
    
    fun toggleLocalAudio(): Boolean {
        val newState = !_isLocalAudioEnabled.value
        rtcEngine?.muteLocalAudioStream(!newState)
        _isLocalAudioEnabled.value = newState
        return newState
    }
    
    fun toggleLocalVideo(): Boolean {
        val newState = !_isLocalVideoEnabled.value
        rtcEngine?.muteLocalVideoStream(!newState)
        _isLocalVideoEnabled.value = newState
        return newState
    }
    
    fun switchCamera() {
        rtcEngine?.switchCamera()
    }
    
    fun enableSpeakerphone(enabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enabled)
    }
    
    fun setAudioRoute(useSpeaker: Boolean) {
        rtcEngine?.setDefaultAudioRoutetoSpeakerphone(useSpeaker)
    }
    
    fun destroy() {
        Log.d(TAG, "🗑️ Destroying RTC Engine")
        leaveChannel()
        rtcEngine?.stopPreview()
        RtcEngine.destroy()
        rtcEngine = null
        isInitialized = false
        _callState.value = CallState.Idle
    }
}
