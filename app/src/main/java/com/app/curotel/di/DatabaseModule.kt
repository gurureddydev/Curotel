package com.app.curotel.di

import android.content.Context
import androidx.room.Room
import com.app.curotel.data.local.CurotelDatabase
import com.app.curotel.data.local.dao.MeasurementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for local database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideCurotelDatabase(
        @ApplicationContext context: Context
    ): CurotelDatabase {
        return Room.databaseBuilder(
            context,
            CurotelDatabase::class.java,
            CurotelDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMeasurementDao(database: CurotelDatabase): MeasurementDao {
        return database.measurementDao()
    }
}
