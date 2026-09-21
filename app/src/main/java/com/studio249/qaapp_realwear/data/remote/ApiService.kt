package com.studio249.qaapp_realwear.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("jobs/new-inspections")
    suspend fun getNewInspections(): Response<JobListResponse>

    @GET("jobs/to-be-fixed")
    suspend fun getToBeFixed(): Response<JobListResponse>

    @GET("jobs/reinspection")
    suspend fun getReinspection(): Response<JobListResponse>

    @GET("jobs/completed")
    suspend fun getCompleted(): Response<JobListResponse>
}
