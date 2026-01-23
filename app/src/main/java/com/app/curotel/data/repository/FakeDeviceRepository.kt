package com.app.curotel.data.repository

import com.app.curotel.domain.model.DeviceConnectionState
import com.app.curotel.domain.model.MeasurementType
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.domain.repository.DeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

// -----------------------------------------------------------------------------
// FAKE IMPLEMENTATION simulating Bluetooth/WebSocket packets
// -----------------------------------------------------------------------------
class FakeDeviceRepository : DeviceRepository {

    private val _connectionState = MutableStateFlow(DeviceConnectionState.DISCONNECTED)
    override val connectionState: Flow<DeviceConnectionState> = _connectionState.asStateFlow()

    private val _activeMeasurement = MutableStateFlow(MeasurementType.NONE)
    override val activeMeasurement: Flow<MeasurementType> = _activeMeasurement.asStateFlow()

    // Simulate a stream that emits packets only when connected and measuring
    override val vitalsStream: Flow<VitalsPacket> = flow {
        while (true) {
            val currentState = _connectionState.value
            val currentMeasurement = _activeMeasurement.value

            if (currentState == DeviceConnectionState.CONNECTED && currentMeasurement != MeasurementType.NONE) {
                emit(generateFakePacket(currentMeasurement))
            }

            // Emit frequency: 500ms (2Hz refresh rate for UI)
            delay(500)
        }
    }

    override suspend fun connect() {
        _connectionState.value = DeviceConnectionState.CONNECTING
        delay(1500) // Simulate generic handshake
        _connectionState.value = DeviceConnectionState.CONNECTED
    }

    override suspend fun disconnect() {
        _connectionState.value = DeviceConnectionState.DISCONNECTED
        _activeMeasurement.value = MeasurementType.NONE
    }

    override suspend fun startMeasurement(type: MeasurementType) {
        if (_connectionState.value == DeviceConnectionState.CONNECTED) {
            _activeMeasurement.value = type
        }
    }

    override suspend fun stopMeasurement() {
        _activeMeasurement.value = MeasurementType.NONE
    }

    private fun generateFakePacket(type: MeasurementType): VitalsPacket {
        return when (type) {
            MeasurementType.THERMOMETER -> {
                // Fluctuates slightly around 36.5 - 37.5
                val temp = 36.5f + Random.nextFloat()
                VitalsPacket(type = type, temperature = temp)
            }
            MeasurementType.OXIMETER -> {
                // SpO2 95-100, HR 60-100
                VitalsPacket(
                    type = type,
                    spo2 = Random.nextInt(96, 100),
                    heartRate = Random.nextInt(65, 85)
                )
            }
            MeasurementType.BP_MONITOR -> {
                // Rising pressure simulation or just stable result for now
                VitalsPacket(
                    type = type,
                    sysBp = Random.nextInt(110, 130),
                    diaBp = Random.nextInt(70, 90),
                    heartRate = Random.nextInt(60, 80)
                )
            }
            else -> VitalsPacket(type = type)
        }
    }
}
