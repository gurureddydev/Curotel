package com.app.curotel.data.remote.websocket

import com.app.curotel.data.remote.dto.VitalsStreamDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket service for real-time vitals streaming
 */
@Singleton
class VitalsWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private var webSocket: WebSocket? = null
    private val vitalsAdapter = moshi.adapter(VitalsStreamDto::class.java)
    
    companion object {
        private const val WS_URL = "wss://api.curotel.com/ws/vitals"
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
    
    /**
     * Connect to WebSocket and emit real-time vitals as Flow
     */
    fun connectToVitalsStream(): Flow<VitalsStreamDto> = callbackFlow {
        val request = Request.Builder()
            .url(WS_URL)
            .build()
        
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Connection established
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    vitalsAdapter.fromJson(text)?.let { vitals ->
                        trySend(vitals)
                    }
                } catch (e: Exception) {
                    // Handle parse error
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Handle binary messages if needed
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }
        
        webSocket = okHttpClient.newWebSocket(request, listener)
        
        awaitClose {
            disconnect()
        }
    }
    
    /**
     * Send a command to the device via WebSocket
     */
    fun sendCommand(command: String) {
        webSocket?.send(command)
    }
    
    /**
     * Disconnect from WebSocket
     */
    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "User disconnected")
        webSocket = null
    }
}
