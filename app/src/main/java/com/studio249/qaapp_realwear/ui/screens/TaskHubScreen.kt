package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.components.VoiceButton

@Composable
fun TaskHubScreen(
    taskId: String,
    onCaptureClick: () -> Unit,
    onReviewClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Task: $taskId",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VoiceButton(
                label = "CAPTURE PHOTO",
                onClick = onCaptureClick,
                modifier = Modifier.weight(1f)
            )
            VoiceButton(
                label = "REVIEW PHOTO",
                onClick = onReviewClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        VoiceButton(
            label = "PREVIOUS PAGE",
            onClick = onBack,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.width(250.dp)
        )
    }
}
