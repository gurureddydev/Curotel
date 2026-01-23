package com.app.curotel.domain.model

enum class DeviceConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class MeasurementType {
    NONE,
    THERMOMETER,
    OXIMETER,
    BP_MONITOR,
    OTOSCOPE,
    STETHOSCOPE
}

sealed class MeasurementState {
    object Idle : MeasurementState()
    object Measuring : MeasurementState()
    data class Success(val results: Map<String, Any>) : MeasurementState() // Generic flexible result map
    data class Error(val message: String) : MeasurementState()
}

// Unified model for streaming vitals (simulating real-time packets)
data class VitalsPacket(
    val type: MeasurementType,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Float? = null,    // Celsius
    val heartRate: Int? = null,        // BPM
    val spo2: Int? = null,             // %
    val sysBp: Int? = null,            // mmHg
    val diaBp: Int? = null,            // mmHg
    val waveform: List<Float>? = null  // Simulating live chart data (e.g. ECG or Pleth)
)
