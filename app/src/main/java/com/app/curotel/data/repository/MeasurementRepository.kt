package com.app.curotel.data.repository

import com.app.curotel.data.local.dao.MeasurementDao
import com.app.curotel.data.local.entity.MeasurementEntity
import com.app.curotel.data.remote.api.CurotelApiService
import com.app.curotel.data.remote.dto.MeasurementDto
import com.app.curotel.domain.model.MeasurementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for measurement operations
 */
interface MeasurementRepository {
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>
    fun getMeasurementsByType(type: MeasurementType): Flow<List<MeasurementEntity>>
    suspend fun getLatestMeasurement(): MeasurementEntity?
    suspend fun saveMeasurement(measurement: MeasurementEntity): Long
    suspend fun deleteMeasurement(id: Long)
    suspend fun syncToCloud(): Result<Unit>
}

/**
 * Implementation of MeasurementRepository
 * Uses Room for local storage and Retrofit for cloud sync
 */
@Singleton
class MeasurementRepositoryImpl @Inject constructor(
    private val measurementDao: MeasurementDao,
    private val apiService: CurotelApiService
) : MeasurementRepository {
    
    override fun getAllMeasurements(): Flow<List<MeasurementEntity>> {
        return measurementDao.getAllMeasurements()
    }
    
    override fun getMeasurementsByType(type: MeasurementType): Flow<List<MeasurementEntity>> {
        return measurementDao.getMeasurementsByType(type.name)
    }
    
    override suspend fun getLatestMeasurement(): MeasurementEntity? {
        return measurementDao.getLatestMeasurement()
    }
    
    override suspend fun saveMeasurement(measurement: MeasurementEntity): Long {
        return measurementDao.insertMeasurement(measurement)
    }
    
    override suspend fun deleteMeasurement(id: Long) {
        measurementDao.deleteMeasurementById(id)
    }
    
    override suspend fun syncToCloud(): Result<Unit> {
        return try {
            val unsyncedMeasurements = measurementDao.getUnsyncedMeasurements()
            if (unsyncedMeasurements.isEmpty()) {
                return Result.success(Unit)
            }
            
            val dtos = unsyncedMeasurements.map { it.toDto() }
            val response = apiService.syncMeasurements(dtos)
            
            if (response.isSuccessful && response.body()?.success == true) {
                measurementDao.markAsSynced(unsyncedMeasurements.map { it.id })
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Mappers ==========
    
    private fun MeasurementEntity.toDto(): MeasurementDto {
        return MeasurementDto(
            id = id.toString(),
            type = type,
            temperature = temperature,
            heartRate = heartRate,
            spo2 = spo2,
            systolicBp = systolicBp,
            diastolicBp = diastolicBp,
            timestamp = timestamp,
            notes = notes
        )
    }
}
