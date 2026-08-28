package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.JobStatus
import com.studio249.qaapp_realwear.model.Step
import java.time.LocalDateTime

object SeedData {
    const val FIXED_LOGIN_STRING = "QAApp"
    const val FAKE_TOKEN = "dummy_session_token_12345"

    val detectList = listOf("Detect 1", "Detect 2", "Detect 3", "Detect 4")

    private fun createSteps(jobId: String): List<Step> = listOf(
        Step(0, "Check Engine Oil", "https://example.com/step1.jpg"),
        Step(1, "Verify Brake Fluid", "https://example.com/step2.jpg"),
        Step(2, "Inspect Tire Pressure", "https://example.com/step3.jpg")
    )

    val newInspectionsJobs = (1..12).map { i ->
        Job(
            id = "OUT-$i",
            title = "New Inspection Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.NewInspections,
            steps = createSteps("OUT-$i")
        )
    }

    val toBeFixedJobs = (1..12).map { i ->
        Job(
            id = "PROG-$i",
            title = "To Be Fixed Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.ToBeFixed,
            steps = createSteps("PROG-$i")
        )
    }

    val reinspectionJobs = (1..12).map { i ->
        Job(
            id = "VER-$i",
            title = "Reinspection Task $i",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.Reinspection,
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
