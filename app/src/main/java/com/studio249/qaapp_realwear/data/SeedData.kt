package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.JobStatus
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.StepStatus
import java.time.LocalDateTime

object SeedData {
    const val FIXED_LOGIN_STRING = "QA249LOGIN"
    const val FAKE_TOKEN = "dummy_session_token_12345"

    val detectList = listOf("Detect 1", "Detect 2", "Detect 3", "Detect 4")

    private fun createSteps(jobId: String): List<Step> = listOf(
        Step(0, "Check Engine Oil", "https://example.com/step1.jpg"),
        Step(1, "Verify Brake Fluid", "https://example.com/step2.jpg"),
        Step(2, "Inspect Tire Pressure", "https://example.com/step3.jpg")
    )

    val outstandingJobs = (1..12).map { i ->
        Job(
            id = "OUT-$i",
            title = "Outstanding Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.Outstanding,
            steps = createSteps("OUT-$i")
        )
    }

    val inProgressJobs = (1..12).map { i ->
        Job(
            id = "PROG-$i",
            title = "In Progress Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.InProgress,
            steps = createSteps("PROG-$i")
        )
    }

    val inVerifyJobs = (1..12).map { i ->
        Job(
            id = "VER-$i",
            title = "In Verify Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.InVerify,
            steps = createSteps("VER-$i")
        )
    }

    val completedJobs = (1..12).map { i ->
        Job(
            id = "COMP-$i",
            title = "Completed Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.Completed,
            steps = createSteps("COMP-$i")
        )
    }
}
