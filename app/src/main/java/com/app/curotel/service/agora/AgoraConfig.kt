package com.app.curotel.service.agora

/**
 * Agora SDK Configuration
 * 
 * YOUR PROJECT HAS APP CERTIFICATE ENABLED - TOKENS ARE REQUIRED!
 * 
 * HOW TO GET A TEMP TOKEN:
 * 1. Go to https://console.agora.io
 * 2. Select your project
 * 3. Click "Generate Temp Token"
 * 4. Enter Channel Name: curotel_demo_room
 * 5. Click Generate
 * 6. Copy the token and paste below
 * 
 * NOTE: Temp tokens expire after 24 hours!
 * For production, you need a backend to generate tokens.
 */
object AgoraConfig {
    
    // ==========================================
    // YOUR AGORA APP ID
    // ==========================================
    const val APP_ID = "ce71399d624c419cacf6799e506e2d80"
    
    // ==========================================
    // PASTE YOUR TEMP TOKEN HERE!
    // Generate from: console.agora.io → Your Project → Generate Temp Token
    // Channel Name: curotel_demo_room
    // ==========================================
    val TEMP_TOKEN: String? = null  // <-- PASTE TOKEN HERE!
    
    // Fixed channel name for demo - both patient and doctor join this channel
    // IMPORTANT: Use this SAME channel name when generating the temp token!
    const val DEMO_CHANNEL = "curotel_demo_room"
    
    /**
     * Video encoding settings - optimized for mobile
     */
    object Video {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val FRAME_RATE = 15
        const val BITRATE = 400
        const val ORIENTATION_MODE = 0 // ADAPTIVE
    }
    
    /**
     * Audio settings
     */
    object Audio {
        const val SAMPLE_RATE = 48000
        const val CHANNELS = 1 // Mono
        const val BITRATE = 48
    }
    
    /**
     * Check if App ID is configured
     */
    fun isConfigured(): Boolean {
        return APP_ID != "YOUR_AGORA_APP_ID" && APP_ID.isNotBlank()
    }
    
    /**
     * Check if token is configured
     */
    fun hasToken(): Boolean {
        return TEMP_TOKEN != null && TEMP_TOKEN != "YOUR_TEMP_TOKEN_HERE" && TEMP_TOKEN.isNotBlank()
    }
}
