package com.app.curotel.di

import com.app.curotel.data.repository.DeviceRepository
import com.app.curotel.data.repository.DeviceRepositoryImpl
import com.app.curotel.data.repository.MeasurementRepository
import com.app.curotel.data.repository.MeasurementRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindMeasurementRepository(
        impl: MeasurementRepositoryImpl
    ): MeasurementRepository
    
    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl
    ): DeviceRepository
}
