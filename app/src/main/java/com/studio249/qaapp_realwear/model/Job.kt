package com.studio249.qaapp_realwear.model

import java.time.LocalDateTime

enum class JobStatus {
    NewInspections, ToBeFixed, Reinspection, Completed
}

data class Job(
    val id: String,
    val title: String,
    val createdAt: LocalDateTime,
    val status: JobStatus,
    val steps: List<Step> = emptyList()
)
