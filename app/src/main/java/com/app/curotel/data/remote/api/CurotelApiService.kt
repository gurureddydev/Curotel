package com.app.curotel.data.remote.api

import com.app.curotel.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for Curotel backend
 */
interface CurotelApiService {
    
    // ========== Measurements ==========
    
    @GET("api/v1/measurements")
    suspend fun getMeasurements(
        @Query("type") type: String? = null,
        @Query("from") fromTimestamp: Long? = null,
        @Query("to") toTimestamp: Long? = null,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<MeasurementDto>>>
    
    @GET("api/v1/measurements/{id}")
    suspend fun getMeasurementById(
        @Path("id") id: String
    ): Response<ApiResponse<MeasurementDto>>
    
    @POST("api/v1/measurements")
    suspend fun createMeasurement(
        @Body measurement: MeasurementDto
    ): Response<ApiResponse<MeasurementDto>>
    
    @POST("api/v1/measurements/sync")
    suspend fun syncMeasurements(
        @Body measurements: List<MeasurementDto>
    ): Response<ApiResponse<List<MeasurementDto>>>
    
    @DELETE("api/v1/measurements/{id}")
    suspend fun deleteMeasurement(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
    
    // ========== Device ==========
    
    @GET("api/v1/device/status")
    suspend fun getDeviceStatus(): Response<ApiResponse<DeviceStatusDto>>
    
    @POST("api/v1/device/pair")
    suspend fun pairDevice(
        @Body deviceInfo: Map<String, String>
    ): Response<ApiResponse<DeviceStatusDto>>
    
    @POST("api/v1/device/unpair")
    suspend fun unpairDevice(): Response<ApiResponse<Unit>>
    
    // ========== Consultations ==========
    
    @GET("api/v1/consultations")
    suspend fun getConsultations(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<ConsultationDto>>>
    
    @POST("api/v1/consultations/start")
    suspend fun startConsultation(
        @Body request: Map<String, String>
    ): Response<ApiResponse<ConsultationDto>>
    
    @POST("api/v1/consultations/{id}/end")
    suspend fun endConsultation(
        @Path("id") id: String
    ): Response<ApiResponse<ConsultationDto>>
    
    // ========== Real-time Streaming ==========
    // Note: WebSocket connections are handled separately
    
    @GET("api/v1/vitals/latest")
    suspend fun getLatestVitals(): Response<ApiResponse<VitalsStreamDto>>
}
