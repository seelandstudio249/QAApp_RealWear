package com.studio249.qaapp_realwear.ui.screens

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.StepStatus
import com.studio249.qaapp_realwear.ui.components.CameraFrameComponent
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.AccentGreen
import com.studio249.qaapp_realwear.ui.theme.BgPrimary
import com.studio249.qaapp_realwear.ui.theme.BgSurface
import com.studio249.qaapp_realwear.ui.theme.BgSurfaceRaised
import com.studio249.qaapp_realwear.ui.theme.DividerColor
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import com.studio249.qaapp_realwear.ui.theme.TextPrimary
import com.studio249.qaapp_realwear.ui.theme.TextSecondary
import com.studio249.qaapp_realwear.utils.StorageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.tooling.preview.Preview as ComposablePreview

enum class InspectionPattern {
    Steps, Capture, Review
}

@Composable
fun NewInspectionsScreen(
    jobId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SeedDataRepository() }

    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var furthestStepIndex by remember { mutableIntStateOf(0) }
    var currentPattern by remember { mutableStateOf(InspectionPattern.Steps) }
    var isLoading by remember { mutableStateOf(true) }
    var detectList by remember { mutableStateOf<List<String>>(emptyList()) }

    var tempCapturedImage by remember { mutableStateOf<File?>(null) }

    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

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
            Text("No inspection steps found", color = TextPrimary)
            RealWearButton(label = "BACK", onClick = onBack)
        }
        return
    }

    val currentStep = steps[currentStepIndex]

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BgPrimary)) {
        if (currentPattern == InspectionPattern.Capture) {
            CameraPreview(
                imageCapture = imageCapture,
                resolutionSelector = resolutionSelector,
                modifier = Modifier.fillMaxSize()
            )

            if (tempCapturedImage != null) {
                AsyncImage(
                    model = tempCapturedImage,
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            CameraFrameComponent(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .aspectRatio(1f)
                    .align(Alignment.Center),
                frameColor = if (tempCapturedImage != null) AccentGreen else AccentBlue,
                showAnimation = tempCapturedImage == null
            )

            if (tempCapturedImage != null) {
                Text(
                    "PHOTO CAPTURED",
                    color = AccentGreen,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            RealWearTopBar(
                title = "STEP ${currentStepIndex + 1} - ${currentStep.title}",
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.Black.copy(alpha = 0.40f),
                rightContent = {
                    Text(
                        text = "STEP ${currentStepIndex + 1} OF ${steps.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            )

            RealWearBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                backgroundColor = Color.Black.copy(alpha = 0.40f)
            ) {
                RealWearButton(
                    label = "BACK",
                    onClick = { currentPattern = InspectionPattern.Steps },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                if (tempCapturedImage != null) {
                    RealWearButton(
                        label = "RETAKE",
                        onClick = { tempCapturedImage = null },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    RealWearButton(
                        label = "CAPTURE",
                        onClick = {
                            takePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                jobId = jobId,
                                onImageCaptured = { file ->
                                    tempCapturedImage = file
                                },
                                onError = {
                                    Log.e("Camera", "Capture failed", it)
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                RealWearButton(
                    label = "VERIFY CAPTURE",
                    onClick = {
                        currentPattern = InspectionPattern.Review
                    },
                    enabled = tempCapturedImage != null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(shadowElevation = 4.dp, color = BgPrimary) {
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds()
                ) {
                    when (currentPattern) {
                        InspectionPattern.Steps -> {
                            AsyncImage(
                                model = currentStep.stepImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        InspectionPattern.Review -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .fillMaxHeight()
                                        .background(BgSurface)
                                ) {
                                    if (tempCapturedImage != null) {
                                        AsyncImage(
                                            model = tempCapturedImage,
                                            contentDescription = "Captured Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text(
                                            "NO IMAGE CAPTURED",
                                            modifier = Modifier.align(Alignment.Center),
                                            color = TextSecondary
                                        )
                                    }

                                    RealWearButton(
                                        label = "RETAKE",
                                        onClick = {
                                            tempCapturedImage = null
                                            currentPattern = InspectionPattern.Capture
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp),
                                        containerColor = Color.Black.copy(alpha = 0.5f),
                                        contentColor = AccentBlue
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .fillMaxHeight()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "DETECT LIST",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn {
                                        items(detectList) { item ->
                                            val isSelected = currentStep.selectedDetectItem == item
                                            Text(
                                                text = item,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (isSelected) AccentBlue.copy(
                                                            alpha = 0.15f
                                                        ) else Color.Transparent
                                                    )
                                                    .then(
                                                        if (isSelected) Modifier.border(
                                                            width = 2.dp,
                                                            color = AccentBlue,
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) else Modifier
                                                    )
                                                    .clickable {
                                                        steps = steps.mapIndexed { i, s ->
                                                            if (i == currentStepIndex) s.copy(
                                                                selectedDetectItem = item
                                                            ) else s
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

                        else -> {}
                    }
                }

                RealWearBottomBar {
                    when (currentPattern) {
                        InspectionPattern.Steps -> {
                            RealWearButton(
                                label = "PREVIOUS STEP",
                                onClick = {
                                    if (currentStepIndex > 0) {
                                        currentStepIndex--
                                        currentPattern = InspectionPattern.Review
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            RealWearButton(
                                label = "NEXT STEP",
                                onClick = {
                                    if (currentStep.status == StepStatus.Verified) {
                                        if (currentStepIndex < steps.size - 1) {
                                            currentStepIndex++
                                            currentPattern = InspectionPattern.Steps
                                        }
                                    } else {
                                        currentPattern = InspectionPattern.Capture
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }

                        InspectionPattern.Review -> {
                            RealWearButton(
                                label = "BACK TO STEPS",
                                onClick = {
                                    currentPattern = InspectionPattern.Steps
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
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
                                        furthestStepIndex =
                                            maxOf(furthestStepIndex, currentStepIndex)
                                        currentPattern = InspectionPattern.Steps
                                        tempCapturedImage = null
                                    }
                                },
                                containerColor = if (isLastStep) AccentGreen else BgSurfaceRaised,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    jobId: String,
    onImageCaptured: (File) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = StorageUtils.getOutputOptions(context, jobId)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile)
            }
        }
    )
}

@ComposablePreview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun NewInspectionsScreenPreview() {
    QAApp_RealwearTheme {
        NewInspectionsScreen(
            jobId = "OUT-1",
            onComplete = {},
            onBack = {}
        )
    }
}
