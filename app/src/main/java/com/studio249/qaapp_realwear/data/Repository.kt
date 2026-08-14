package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.Step
import java.io.File

interface Repository {
    suspend fun postLogin(loginString: String): Result<String>
    suspend fun getOutstandingList(): Result<List<Job>>
    suspend fun getInProgressList(): Result<List<Job>>
    suspend fun getInVerifyList(): Result<List<Job>>
    suspend fun getCompletedList(): Result<List<Job>>
    suspend fun getProcedures(jobId: String): Result<List<Step>>
    suspend fun getInProgressProcedures(jobId: String): Result<List<Step>>
    suspend fun getInVerifyProcedures(jobId: String): Result<List<Step>>
    suspend fun postVerifyImage(image: File): Result<String>
    suspend fun postUpdateVerifyImage(detectItem: String?): Result<Unit>
    suspend fun postUpdateInProgressJob(image: File, stepIndex: Int, status: String): Result<Unit>
    suspend fun postUpdateInVerifyJob(stepIndex: Int, status: String): Result<Unit>
    suspend fun getDetectList(): Result<List<String>>
    suspend fun completeJob(jobId: String): Result<Unit>
}
