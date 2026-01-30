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
    // YOUR AGORA APP ID (Already configured ✓)
    // ==========================================
    const val APP_ID = "ce71399d624c419cacf6799e506e2d80"
    
    // ==========================================
    // AGORA CHAT APP KEY
    // Format: "orgname#appname"
    // Get from: console.agora.io → Project → Chat → Enable → Copy App Key
    // ==========================================
    const val CHAT_APP_KEY = "4110013852#1649808"  // <-- REPLACE with real value!
    
    // ==========================================
    // RTC VIDEO TOKEN
    // Generate from: console.agora.io → Build → Temp Token
    // Channel Name: curotel_demo_room
    // ==========================================
    val TEMP_TOKEN: String? = "007eJxTYJBdf+/4k/LtTr8YZ3AcvpviWh4xTfVa3OO9WXPP1cn0HNRVYEhONTc0trRMMTMySTYxtExOTE4zM7e0TDU1MEs1SrEw+NBTntkQyMig1WPKzMgAgSC+IENyaVF+SWpOfEpqbn58UX5+LgMDAJXXJfc="  // <-- Paste RTC token here
    
    // ==========================================
    // CHAT TOKENS (for local testing)
    // Generate from: console.agora.io → Chat → Temp Token Generator
    // ==========================================
    // Token for patient1
    val CHAT_TOKEN_PATIENT: String? = "007eJxTYDj4tmXNxOKVRVXH9BJs5TvXBarkN72pF004/qsiLkpVJEeBITnV3NDY0jLFzMgk2cTQMjkxOc3M3NIy1dTALNUoxcLgcWd5ZkMgI4PQ4wpmRgZWBkYgBPFVGEzMUgxTE5MNdNMSk5N1DQ3TDHSTkpIsdM2MLUyMLQwTk4wNUgF5eieX"
    
    // Token for doctor1
    val CHAT_TOKEN_DOCTOR: String? = "007eJxTYPj0/LN8eRtvv3D32ocp8ZYzT38PksvwWDrjLFPu2iXec24qMCSnmhsaW1qmmBmZJJsYWiYnJqeZmVtappoamKUapVgYeHeWZzYEMjLYvnJhZmRgZWAEQhBfhcEk1cLC0NLMQDctMTlZ19AwzUDXwtjEQtfA0jTNxAiIDFKTANE0J58="
    
    // ==========================================
    // DEMO USER IDs (Create in Console → Chat → User Management)
    // ==========================================
    const val DEMO_USER_PATIENT = "patient1"
    const val DEMO_USER_DOCTOR = "doctor1"
    
    // App Certificate (optional - for local token generation)
    val APP_CERTIFICATE: String? = null
    
    // Fixed channel name for demo - both participants join this channel
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
    
    /**
     * Check if Chat is properly configured
     */
    fun isChatConfigured(): Boolean {
        return CHAT_APP_KEY != "YOUR_ORG#YOUR_APP" && 
               CHAT_APP_KEY.contains("#") && 
               CHAT_APP_KEY.isNotBlank()
    }
}
