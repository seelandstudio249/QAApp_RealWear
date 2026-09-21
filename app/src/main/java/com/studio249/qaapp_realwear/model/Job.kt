package com.studio249.qaapp_realwear.model

import java.time.LocalDateTime

enum class JobStatus {
    NewInspections, ToBeFixed, Reinspection, Completed
}

data class Job(
    val id: String,
    val vehicleId: String = "",
    val vehiclePlateNo: String = "",
    val clientName: String = "",
    val title: String = "", // Legacy title support
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: JobStatus,
    val defectId: String? = null,
    val defectType: String? = null,
    val defectStatus: String? = null,
    val completedAt: LocalDateTime? = null,
    val steps: List<Step> = emptyList()
)
