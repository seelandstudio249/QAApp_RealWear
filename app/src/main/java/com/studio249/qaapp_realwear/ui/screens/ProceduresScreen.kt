package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.StepStatus
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.*

enum class ProcedurePattern {
    Steps, Capture, Review
}

@Composable
fun ProceduresScreen(
    jobId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { SeedDataRepository() }
    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var furthestStepIndex by remember { mutableIntStateOf(0) }
    var currentPattern by remember { mutableStateOf(ProcedurePattern.Steps) }
    var isLoading by remember { mutableStateOf(true) }
    var detectList by remember { mutableStateOf<List<String>>(emptyList()) }

    // State for captured image in current session
    var tempCapturedImage by remember { mutableStateOf<java.io.File?>(null) }

    LaunchedEffect(jobId) {
        val stepsResult = repository.getProcedures(jobId)
        steps = stepsResult.getOrDefault(emptyList())
        val detectResult = repository.getDetectList()
        detectList = detectResult.getOrDefault(emptyList())
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
            Text("No procedures found", color = TextPrimary)
            RealWearButton(label = "BACK", onClick = onBack)
        }
        return
    }

    val currentStep = steps[currentStepIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
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

        // Content Area - weight(1f) ensures it takes up all available space between bars
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (currentPattern) {
                ProcedurePattern.Steps -> {
                    AsyncImage(
                        model = currentStep.stepImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                ProcedurePattern.Capture -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        // Viewfinder Simulation
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(2.dp, AccentBlue, RoundedCornerShape(8.dp))
                        )
                        if (tempCapturedImage != null) {
                            Text("PHOTO CAPTURED", color = AccentGreen, modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
                        } else {
                            Text("VIEWFINDER", color = AccentBlue)
                        }
                    }
                }
                ProcedurePattern.Review -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left 70%: Captured Image
                        Box(modifier = Modifier.weight(0.7f).fillMaxHeight().background(BgSurface)) {
                            Text("CAPTURED IMAGE PREVIEW", modifier = Modifier.align(Alignment.Center), color = TextSecondary)
                            
                            RealWearButton(
                                label = "RETAKE",
                                onClick = { 
                                    tempCapturedImage = null
                                    currentPattern = ProcedurePattern.Capture 
                                },
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                containerColor = Color.Transparent,
                                contentColor = AccentBlue
                            )
                        }
                        
                        // Right 30%: Detect List
                        Column(modifier = Modifier.weight(0.3f).fillMaxHeight().padding(8.dp)) {
                            Text("DETECT LIST", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn {
                                items(detectList) { item ->
                                    val isSelected = currentStep.selectedDetectItem == item
                                    Text(
                                        text = item,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                            .then(if (isSelected) Modifier.border(width = 2.dp, color = AccentBlue, shape = RoundedCornerShape(4.dp)) else Modifier)
                                            .clickable { 
                                                steps = steps.mapIndexed { i, s ->
                                                    if (i == currentStepIndex) s.copy(selectedDetectItem = item) else s
                                                }
                                            }
                                            .padding(12.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Bar - Using RealWearBottomBar to ensure equal and full-height buttons
        RealWearBottomBar {
            when (currentPattern) {
                ProcedurePattern.Steps -> {
                    RealWearButton(
                        label = "PREVIOUS STEP",
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                                currentPattern = ProcedurePattern.Review
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    RealWearButton(
                        label = "NEXT STEP",
                        onClick = {
                            if (currentStep.status == StepStatus.Verified) {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                    currentPattern = ProcedurePattern.Steps
                                }
                            } else {
                                currentPattern = ProcedurePattern.Capture
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                ProcedurePattern.Capture -> {
                    RealWearButton(
                        label = "CAPTURE AGAIN",
                        onClick = { tempCapturedImage = null },
                        enabled = tempCapturedImage != null,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    RealWearButton(
                        label = "CAPTURE",
                        onClick = { tempCapturedImage = java.io.File("dummy.jpg") },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    RealWearButton(
                        label = "VERIFY CAPTURE",
                        onClick = {
                            currentPattern = ProcedurePattern.Review
                        },
                        enabled = tempCapturedImage != null,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                ProcedurePattern.Review -> {
                    RealWearButton(
                        label = "PREVIOUS STEP",
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                                currentPattern = ProcedurePattern.Review
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    val isLastStep = currentStepIndex == steps.size - 1
                    RealWearButton(
                        label = if (isLastStep) "COMPLETE JOB" else "NEXT STEP",
                        onClick = {
                            steps = steps.mapIndexed { i, s ->
                                if (i == currentStepIndex) s.copy(status = StepStatus.Verified) else s
                            }
                            if (isLastStep) {
                                onComplete()
                            } else {
                                currentStepIndex++
                                furthestStepIndex = maxOf(furthestStepIndex, currentStepIndex)
                                currentPattern = ProcedurePattern.Steps
                            }
                        },
                        containerColor = if (isLastStep) AccentGreen else BgSurfaceRaised,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun ProceduresScreenPreview() {
    QAApp_RealwearTheme {
        ProceduresScreen(
            jobId = "OUT-1",
            onComplete = {},
            onBack = {}
        )
    }
}
