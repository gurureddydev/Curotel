package com.app.curotel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.curotel.data.local.dao.MeasurementDao
import com.app.curotel.data.local.entity.MeasurementEntity

/**
 * Room Database for Curotel app
 */
@Database(
    entities = [MeasurementEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CurotelDatabase : RoomDatabase() {
    
    abstract fun measurementDao(): MeasurementDao
    
    companion object {
        const val DATABASE_NAME = "curotel_db"
    }
}
