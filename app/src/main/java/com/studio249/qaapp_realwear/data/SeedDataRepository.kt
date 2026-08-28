package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.Step
import kotlinx.coroutines.delay
import java.io.File

class SeedDataRepository : Repository {
    private val simulateDelay: Long = 500

    override suspend fun postLogin(loginString: String): Result<String> {
        delay(simulateDelay)
        return if (loginString == SeedData.FIXED_LOGIN_STRING) {
            Result.success(SeedData.FAKE_TOKEN)
        } else {
            Result.failure(Exception("Login Fail"))
        }
    }

    override suspend fun getNewInspectionsList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.newInspectionsJobs)
    }

    override suspend fun getToBeFixedList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.toBeFixedJobs)
    }

    override suspend fun getReinspectionList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.reinspectionJobs)
    }

    override suspend fun getCompletedList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.completedJobs)
    }

    override suspend fun getProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.newInspectionsJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun getToBeFixedProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.toBeFixedJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun getReinspectionProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.reinspectionJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun postVerifyImage(image: File): Result<String> {
        delay(simulateDelay)
        return Result.success("https://example.com/verified_image.jpg")
    }

    override suspend fun postUpdateVerifyImage(detectItem: String?): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }

    override suspend fun postUpdateToBeFixedJob(image: File, stepIndex: Int, status: String): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }

    override suspend fun postUpdateReinspectionJob(stepIndex: Int, status: String): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }

    override suspend fun getDetectList(): Result<List<String>> {
        delay(simulateDelay)
        return Result.success(SeedData.detectList)
    }

    override suspend fun completeJob(jobId: String): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }
}
