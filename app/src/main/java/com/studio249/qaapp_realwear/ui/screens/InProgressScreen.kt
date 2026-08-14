package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.*
import java.io.File

@Composable
fun InProgressScreen(
    jobId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { SeedDataRepository() }
    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isCameraActive by remember { mutableStateOf(false) }
    var tempCapturedImage by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(jobId) {
        val result = repository.getInProgressProcedures(jobId)
        steps = result.getOrDefault(emptyList())
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    if (steps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No steps found", color = TextPrimary)
            RealWearButton(label = "BACK", onClick = onBack)
        }
        return
    }

    val currentStep = steps[currentStepIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        RealWearTopBar(
            title = "STEP ${currentStepIndex + 1} - ${currentStep.title}",
            rightContent = {
                Text(
                    text = "STEP ${currentStepIndex + 1} OF ${steps.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(0.76f)) {
            if (isCameraActive) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .border(2.dp, AccentBlue, RoundedCornerShape(8.dp))
                    )
                    if (tempCapturedImage != null) {
                        Text("PHOTO CAPTURED", color = AccentGreen, modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
                    } else {
                        Text("VIEWFINDER", color = AccentBlue)
                    }
                }
            } else {
                AsyncImage(
                    model = currentStep.stepImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.14f)
                .background(BgSurface)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCameraActive) {
                Spacer(modifier = Modifier.weight(1f))
                RealWearButton(
                    label = "DEFECTS FIXED",
                    onClick = { isCameraActive = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                RealWearButton(
                    label = "CAPTURE AGAIN",
                    onClick = { tempCapturedImage = null },
                    enabled = tempCapturedImage != null,
                    modifier = Modifier.weight(1f)
                )
                RealWearButton(
                    label = "CAPTURE",
                    onClick = { tempCapturedImage = File("dummy.jpg") },
                    modifier = Modifier.weight(1f)
                )
                RealWearButton(
                    label = "VERIFY CAPTURE",
                    onClick = {
                        // Simulate API call PostUpdateInProgressJob
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                            tempCapturedImage = null
                            isCameraActive = false
                        } else {
                            onComplete()
                        }
                    },
                    enabled = tempCapturedImage != null,
                    containerColor = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun InProgressScreenPreview() {
    QAApp_RealwearTheme {
        InProgressScreen(
            jobId = "PROG-1",
            onComplete = {},
            onBack = {}
        )
    }
}
