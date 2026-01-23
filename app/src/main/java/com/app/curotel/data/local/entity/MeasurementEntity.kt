package com.app.curotel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing measurement records
 */
@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val type: String, // THERMOMETER, OXIMETER, BP_MONITOR, STETHOSCOPE, OTOSCOPE
    
    // Measurement values
    val temperature: Float? = null,      // Celsius
    val heartRate: Int? = null,          // BPM
    val spo2: Int? = null,               // %
    val systolicBp: Int? = null,         // mmHg
    val diastolicBp: Int? = null,        // mmHg
    
    // Metadata
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val deviceId: String? = null,
    val syncedToCloud: Boolean = false
)
