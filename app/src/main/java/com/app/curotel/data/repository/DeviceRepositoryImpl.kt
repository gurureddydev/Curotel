package com.app.curotel.data.repository

import com.app.curotel.data.remote.api.CurotelApiService
import com.app.curotel.data.remote.websocket.VitalsWebSocketService
import com.app.curotel.data.remote.dto.VitalsStreamDto
import com.app.curotel.domain.model.DeviceConnectionState
import com.app.curotel.domain.model.MeasurementType
import com.app.curotel.domain.model.VitalsPacket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository interface for device operations
 */
interface DeviceRepository {
    val connectionState: StateFlow<DeviceConnectionState>
    val activeMeasurement: StateFlow<MeasurementType>
    val currentVitals: StateFlow<VitalsPacket>
    
    suspend fun connect()
    suspend fun disconnect()
    suspend fun startMeasurement(type: MeasurementType)
    suspend fun stopMeasurement()
    fun getVitalsStream(): Flow<VitalsStreamDto>
}

/**
 * Implementation of DeviceRepository
 * Handles device connection and real-time vitals streaming
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val apiService: CurotelApiService,
    private val webSocketService: VitalsWebSocketService
) : DeviceRepository {
    
    private val _connectionState = MutableStateFlow(DeviceConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()
    
    private val _activeMeasurement = MutableStateFlow(MeasurementType.NONE)
    override val activeMeasurement: StateFlow<MeasurementType> = _activeMeasurement.asStateFlow()
    
    private val _currentVitals = MutableStateFlow(VitalsPacket(MeasurementType.NONE))
    override val currentVitals: StateFlow<VitalsPacket> = _currentVitals.asStateFlow()
    
    override suspend fun connect() {
        _connectionState.value = DeviceConnectionState.CONNECTING
        delay(1500) // Simulate connection time
        _connectionState.value = DeviceConnectionState.CONNECTED
    }
    
    override suspend fun disconnect() {
        webSocketService.disconnect()
        _connectionState.value = DeviceConnectionState.DISCONNECTED
        _activeMeasurement.value = MeasurementType.NONE
    }
    
    override suspend fun startMeasurement(type: MeasurementType) {
        _activeMeasurement.value = type
        
        // Start simulating vitals based on type
        simulateVitals(type)
    }
    
    override suspend fun stopMeasurement() {
        _activeMeasurement.value = MeasurementType.NONE
        _currentVitals.value = VitalsPacket(MeasurementType.NONE)
    }
    
    override fun getVitalsStream(): Flow<VitalsStreamDto> {
        return webSocketService.connectToVitalsStream()
    }
    
    /**
     * Simulate real-time vitals for demo purposes
     * In production, this would be replaced with actual device data
     */
    private suspend fun simulateVitals(type: MeasurementType) {
        while (_activeMeasurement.value == type) {
            val packet = when (type) {
                MeasurementType.THERMOMETER -> VitalsPacket(
                    type = type,
                    temperature = 36.5f + Random.nextFloat() * 0.8f
                )
                MeasurementType.OXIMETER -> VitalsPacket(
                    type = type,
                    spo2 = 95 + Random.nextInt(5),
                    heartRate = 65 + Random.nextInt(20),
                    waveform = generatePlethWaveform()
                )
                MeasurementType.BP_MONITOR -> VitalsPacket(
                    type = type,
                    sysBp = 115 + Random.nextInt(15),
                    diaBp = 75 + Random.nextInt(10),
                    heartRate = 70 + Random.nextInt(15)
                )
                MeasurementType.STETHOSCOPE -> VitalsPacket(
                    type = type,
                    heartRate = 70 + Random.nextInt(15),
                    waveform = generateHeartWaveform()
                )
                else -> VitalsPacket(type = type)
            }
            
            _currentVitals.value = packet
            delay(1000) // Update every second
        }
    }
    
    private fun generatePlethWaveform(): List<Float> {
        return (0..100).map { i ->
            val x = i / 100f * 2 * Math.PI
            (kotlin.math.sin(x) * 0.5f + kotlin.math.sin(x * 2) * 0.3f).toFloat()
        }
    }
    
    private fun generateHeartWaveform(): List<Float> {
        val waveform = mutableListOf<Float>()
        repeat(100) { i ->
            val t = i / 100f
            val y = when {
                t < 0.1f -> kotlin.math.sin(t * Math.PI / 0.1f).toFloat() * 0.8f
                t < 0.15f -> 0f
                t < 0.25f -> kotlin.math.sin((t - 0.15f) * Math.PI / 0.1f).toFloat() * 0.4f
                else -> 0f
            }
            waveform.add(y)
        }
        return waveform
    }
}
