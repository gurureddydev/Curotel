package com.app.curotel.data.local.dao

import androidx.room.*
import com.app.curotel.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for measurement database operations
 */
@Dao
interface MeasurementDao {
    
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE type = :type ORDER BY timestamp DESC")
    fun getMeasurementsByType(type: String): Flow<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMeasurementsInRange(startTime: Long, endTime: Long): Flow<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): MeasurementEntity?
    
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurement(): MeasurementEntity?
    
    @Query("SELECT * FROM measurements WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurementByType(type: String): MeasurementEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)
    
    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity)
    
    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)
    
    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)
    
    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()
    
    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun getMeasurementCount(): Int
    
    @Query("SELECT * FROM measurements WHERE syncedToCloud = 0")
    suspend fun getUnsyncedMeasurements(): List<MeasurementEntity>
    
    @Query("UPDATE measurements SET syncedToCloud = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}
