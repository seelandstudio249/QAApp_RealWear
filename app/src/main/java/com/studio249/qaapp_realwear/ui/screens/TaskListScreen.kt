package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio249.qaapp_realwear.data.MockData
import com.studio249.qaapp_realwear.model.Task
import com.studio249.qaapp_realwear.model.TaskStatus
import com.studio249.qaapp_realwear.ui.components.VoiceCard
import java.time.format.DateTimeFormatter

@Composable
fun TaskListScreen(onTaskSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tasks List",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MockData.tasks) { task ->
                TaskItem(task = task, onClick = { onTaskSelected(task.id) })
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    val statusColor = when (task.status) {
        TaskStatus.Outstanding -> Color.Gray
        TaskStatus.InProgress -> Color(0xFFFFD700) // Yellow
        TaskStatus.Complete -> Color(0xFF2E7D32) // Green
    }

    VoiceCard(
        label = "SELECT ${task.id}",
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = task.id,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Surface(
                color = statusColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = task.status.name.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
