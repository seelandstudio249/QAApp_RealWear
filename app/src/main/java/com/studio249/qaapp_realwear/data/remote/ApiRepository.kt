package com.studio249.qaapp_realwear.data.remote

import com.studio249.qaapp_realwear.data.Repository
import com.studio249.qaapp_realwear.data.SeedData
import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.JobStatus
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.User
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val apiService: ApiService
) : Repository {

    override suspend fun postLogin(loginString: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(qrToken = loginString))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Result.success(body.data)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNewInspectionsList(): Result<List<Job>> {
        return fetchJobList(JobStatus.NewInspections) { apiService.getNewInspections() }
    }

    override suspend fun getToBeFixedList(): Result<List<Job>> {
        return fetchJobList(JobStatus.ToBeFixed) { apiService.getToBeFixed() }
    }

    override suspend fun getReinspectionList(): Result<List<Job>> {
        return fetchJobList(JobStatus.Reinspection) { apiService.getReinspection() }
    }

    override suspend fun getCompletedList(): Result<List<Job>> {
        return fetchJobList(JobStatus.Completed) { apiService.getCompleted() }
    }

    private suspend fun fetchJobList(
        status: JobStatus,
        call: suspend () -> Response<JobListResponse>
    ): Result<List<Job>> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Result.success(body.data.map { it.toDomain(status) })
            } else {
                Result.failure(Exception("Error Code: ${response.code()}\nFailed to fetch jobs: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun RemoteJob.toDomain(status: JobStatus): Job {
        return Job(
            id = id,
            vehicleId = vehicleId ?: "",
            vehiclePlateNo = vehiclePlateNo ?: "",
            clientName = clientName ?: "",
            title = vehiclePlateNo ?: id,
            createdAt = createdAt?.let { parseDate(it) } ?: LocalDateTime.now(),
            status = status,
            defectId = defectId,
            defectType = defectType,
            defectStatus = defectStatus,
            completedAt = completedAt?.let { parseDate(it) },
            steps = emptyList() // Procedures will fetch steps separately
        )
    }

    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            ZonedDateTime.parse(dateStr).toLocalDateTime()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    // Delegating to SeedData for now to keep the app functional while other APIs are implemented
    override suspend fun getProcedures(jobId: String): Result<List<Step>> = 
        Result.success(SeedData.newInspectionsJobs.find { it.id == jobId }?.steps ?: emptyList())
        
    override suspend fun getToBeFixedProcedures(jobId: String): Result<List<Step>> = 
        Result.success(SeedData.toBeFixedJobs.find { it.id == jobId }?.steps ?: emptyList())
        
    override suspend fun getReinspectionProcedures(jobId: String): Result<List<Step>> = 
        Result.success(SeedData.reinspectionJobs.find { it.id == jobId }?.steps ?: emptyList())

    override suspend fun postVerifyImage(image: File): Result<String> = 
        Result.success("https://example.com/verified_image.jpg")

    override suspend fun postUpdateVerifyImage(detectItem: String?): Result<Unit> = Result.success(Unit)
    
    override suspend fun postUpdateToBeFixedJob(image: File, stepIndex: Int, status: String): Result<Unit> = 
        Result.success(Unit)
        
    override suspend fun postUpdateReinspectionJob(stepIndex: Int, status: String): Result<Unit> = 
        Result.success(Unit)

    override suspend fun getDetectList(): Result<List<String>> = Result.success(SeedData.detectList)
    
    override suspend fun completeJob(jobId: String): Result<Unit> = Result.success(Unit)
}
