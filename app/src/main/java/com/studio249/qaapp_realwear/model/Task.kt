package com.studio249.qaapp_realwear.model

import java.time.LocalDateTime

enum class TaskStatus {
    Outstanding, InProgress, Complete
}

data class Task(
    val id: String,
    val dateTime: LocalDateTime,
    val status: TaskStatus = TaskStatus.Outstanding
)
