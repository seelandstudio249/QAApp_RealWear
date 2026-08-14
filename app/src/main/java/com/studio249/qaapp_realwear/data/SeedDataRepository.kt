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

    override suspend fun getOutstandingList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.outstandingJobs)
    }

    override suspend fun getInProgressList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.inProgressJobs)
    }

    override suspend fun getInVerifyList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.inVerifyJobs)
    }

    override suspend fun getCompletedList(): Result<List<Job>> {
        delay(simulateDelay)
        return Result.success(SeedData.completedJobs)
    }

    override suspend fun getProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.outstandingJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun getInProgressProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.inProgressJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun getInVerifyProcedures(jobId: String): Result<List<Step>> {
        delay(simulateDelay)
        return Result.success(SeedData.inVerifyJobs.find { it.id == jobId }?.steps ?: emptyList())
    }

    override suspend fun postVerifyImage(image: File): Result<String> {
        delay(simulateDelay)
        return Result.success("https://example.com/verified_image.jpg")
    }

    override suspend fun postUpdateVerifyImage(detectItem: String?): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }

    override suspend fun postUpdateInProgressJob(image: File, stepIndex: Int, status: String): Result<Unit> {
        delay(simulateDelay)
        return Result.success(Unit)
    }

    override suspend fun postUpdateInVerifyJob(stepIndex: Int, status: String): Result<Unit> {
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
