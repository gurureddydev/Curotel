package com.app.curotel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.curotel.data.local.entity.MeasurementEntity
import com.app.curotel.data.local.preferences.UserPreferencesManager
import com.app.curotel.data.repository.DeviceRepository
import com.app.curotel.data.repository.MeasurementRepository
import com.app.curotel.domain.model.DeviceConnectionState
import com.app.curotel.domain.model.MeasurementType
import com.app.curotel.domain.model.VitalsPacket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for device and measurement operations
 * Uses Hilt for dependency injection
 */
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val measurementRepository: MeasurementRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {
    
    // ========== Device State ==========
    
    val connectionState: StateFlow<DeviceConnectionState> = deviceRepository.connectionState
    
    val activeMeasurement: StateFlow<MeasurementType> = deviceRepository.activeMeasurement
    
    val currentVitals: StateFlow<VitalsPacket> = deviceRepository.currentVitals
    
    // ========== Measurements ==========
    
    val measurements: StateFlow<List<MeasurementEntity>> = measurementRepository
        .getAllMeasurements()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== Preferences ==========
    
    val isOnboardingComplete: StateFlow<Boolean> = preferencesManager
        .isOnboardingComplete
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val autoConnectEnabled: StateFlow<Boolean> = preferencesManager
        .autoConnect
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    // ========== UI State ==========
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    
    // ========== Device Operations ==========
    
    fun connectDevice() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                deviceRepository.connect()
            } catch (e: Exception) {
                _errorMessage.emit("Failed to connect: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun disconnectDevice() {
        viewModelScope.launch {
            try {
                deviceRepository.disconnect()
            } catch (e: Exception) {
                _errorMessage.emit("Failed to disconnect: ${e.message}")
            }
        }
    }
    
    // ========== Measurement Operations ==========
    
    fun startMeasurement(type: MeasurementType) {
        viewModelScope.launch {
            try {
                deviceRepository.startMeasurement(type)
            } catch (e: Exception) {
                _errorMessage.emit("Failed to start measurement: ${e.message}")
            }
        }
    }
    
    fun stopMeasurement() {
        viewModelScope.launch {
            try {
                deviceRepository.stopMeasurement()
            } catch (e: Exception) {
                _errorMessage.emit("Failed to stop measurement: ${e.message}")
            }
        }
    }
    
    fun saveMeasurement(vitals: VitalsPacket) {
        viewModelScope.launch {
            try {
                val entity = MeasurementEntity(
                    type = vitals.type.name,
                    temperature = vitals.temperature,
                    heartRate = vitals.heartRate,
                    spo2 = vitals.spo2,
                    systolicBp = vitals.sysBp,
                    diastolicBp = vitals.diaBp
                )
                measurementRepository.saveMeasurement(entity)
            } catch (e: Exception) {
                _errorMessage.emit("Failed to save measurement: ${e.message}")
            }
        }
    }
    
    fun syncMeasurements() {
        viewModelScope.launch {
            _isLoading.value = true
            measurementRepository.syncToCloud()
                .onSuccess {
                    // Sync successful
                }
                .onFailure { e ->
                    _errorMessage.emit("Sync failed: ${e.message}")
                }
            _isLoading.value = false
        }
    }
    
    // ========== Preferences Operations ==========
    
    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingComplete(true)
        }
    }
    
    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoConnect(enabled)
        }
    }
}
