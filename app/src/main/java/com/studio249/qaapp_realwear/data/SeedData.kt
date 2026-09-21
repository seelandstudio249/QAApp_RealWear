package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.JobStatus
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.User
import java.time.LocalDateTime

object SeedData {
    const val FIXED_LOGIN_STRING = "QAApp"
    const val FAKE_TOKEN = "dummy_session_token_12345"

    val FAKE_USER = User(
        id = "11111111-1111-1111-1111-111111111111",
        username = "45631278",
        fullname = "Testing cert",
        usergroups = listOf("Inspector"),
        authToken = "RW-1fc83829-72af-47a7-a519-9ec1a95fe532"
    )

    val detectList = listOf("Detect 1", "Detect 2", "Detect 3", "Detect 4")

    private fun createSteps(jobId: String): List<Step> = listOf(
        Step(0, "Check Engine Oil", "https://example.com/step1.jpg"),
        Step(1, "Verify Brake Fluid", "https://example.com/step2.jpg"),
        Step(2, "Inspect Tire Pressure", "https://example.com/step3.jpg")
    )

    val newInspectionsJobs = (1..12).map { i ->
        Job(
            id = "OUT-$i",
            vehiclePlateNo = "SBA${1000 + i}A",
            clientName = "ST Engineering",
            title = "SBA${1000 + i}A",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.NewInspections,
            steps = createSteps("OUT-$i")
        )
    }

    val toBeFixedJobs = (1..12).map { i ->
        Job(
            id = "PROG-$i",
            vehiclePlateNo = "SCD${2000 + i}B",
            clientName = "PSA Singapore",
            title = "SCD${2000 + i}B",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.ToBeFixed,
            steps = createSteps("PROG-$i")
        )
    }

    val reinspectionJobs = (1..12).map { i ->
        Job(
            id = "VER-$i",
            vehiclePlateNo = "SFE${3000 + i}C",
            clientName = "ComfortDelGro",
            title = "SFE${3000 + i}C",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.Reinspection,
            steps = createSteps("VER-$i")
        )
    }

    val completedJobs = (1..12).map { i ->
        Job(
            id = "COMP-$i",
            vehiclePlateNo = "SGH${4000 + i}D",
            clientName = "SMRT",
            title = "SGH${4000 + i}D",
            createdAt = LocalDateTime.now().minusDays(i.toLong()),
            status = JobStatus.Completed,
            steps = createSteps("COMP-$i")
        )
    }
}
