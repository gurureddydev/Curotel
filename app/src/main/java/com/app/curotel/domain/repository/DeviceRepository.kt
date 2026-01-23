package com.app.curotel.domain.repository

import com.app.curotel.domain.model.DeviceConnectionState
import com.app.curotel.domain.model.MeasurementState
import com.app.curotel.domain.model.MeasurementType
import com.app.curotel.domain.model.VitalsPacket
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    
    // Monitors connection status of the CURO device
    val connectionState: Flow<DeviceConnectionState>

    // Monitors which sensor is currently active
    val activeMeasurement: Flow<MeasurementType>

    // Real-time stream of sensor data
    val vitalsStream: Flow<VitalsPacket>

    // Control methods
    suspend fun connect()
    suspend fun disconnect()
    
    suspend fun startMeasurement(type: MeasurementType)
    suspend fun stopMeasurement()
}
