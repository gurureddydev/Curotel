package com.app.curotel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for measurement data from API
 */
@JsonClass(generateAdapter = true)
data class MeasurementDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "heart_rate") val heartRate: Int? = null,
    @Json(name = "spo2") val spo2: Int? = null,
    @Json(name = "systolic_bp") val systolicBp: Int? = null,
    @Json(name = "diastolic_bp") val diastolicBp: Int? = null,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "notes") val notes: String? = null
)

/**
 * DTO for device connection status
 */
@JsonClass(generateAdapter = true)
data class DeviceStatusDto(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "is_connected") val isConnected: Boolean,
    @Json(name = "battery_level") val batteryLevel: Int,
    @Json(name = "firmware_version") val firmwareVersion: String,
    @Json(name = "last_sync") val lastSync: Long?
)

/**
 * DTO for real-time vitals stream
 */
@JsonClass(generateAdapter = true)
data class VitalsStreamDto(
    @Json(name = "type") val type: String,
    @Json(name = "value") val value: Float,
    @Json(name = "unit") val unit: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "waveform") val waveform: List<Float>? = null
)

/**
 * Generic API response wrapper
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T?,
    @Json(name = "message") val message: String?,
    @Json(name = "error_code") val errorCode: String? = null
)

/**
 * DTO for consultation request
 */
@JsonClass(generateAdapter = true)
data class ConsultationDto(
    @Json(name = "id") val id: String,
    @Json(name = "doctor_id") val doctorId: String,
    @Json(name = "doctor_name") val doctorName: String,
    @Json(name = "specialty") val specialty: String,
    @Json(name = "status") val status: String, // PENDING, ACTIVE, COMPLETED
    @Json(name = "video_token") val videoToken: String? = null,
    @Json(name = "start_time") val startTime: Long,
    @Json(name = "end_time") val endTime: Long? = null
)
