package com.studio249.qaapp_realwear.data

import com.studio249.qaapp_realwear.model.Task
import com.studio249.qaapp_realwear.model.TaskStatus
import com.studio249.qaapp_realwear.model.User
import java.time.LocalDateTime

object MockData {
    val users = listOf(
        User(id = "1", name = "Admin"),
        User(id = "2", name = "Inspector")
    )

    val tasks = listOf(
        Task(
            id = "TASK-003",
            dateTime = LocalDateTime.now().minusHours(1),
            status = TaskStatus.Outstanding
        ),
        Task(
            id = "TASK-002",
            dateTime = LocalDateTime.now().minusDays(1),
            status = TaskStatus.InProgress
        ),
        Task(
            id = "TASK-001",
            dateTime = LocalDateTime.now().minusDays(2),
            status = TaskStatus.Complete
        )
    ).sortedByDescending { it.dateTime }
}
