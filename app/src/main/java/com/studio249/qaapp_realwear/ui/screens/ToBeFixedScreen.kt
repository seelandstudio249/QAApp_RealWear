package com.studio249.qaapp_realwear.ui.screens

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.*
import com.studio249.qaapp_realwear.utils.StorageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ToBeFixedScreen(
    jobId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SeedDataRepository() }
    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isCameraActive by remember { mutableStateOf(false) }

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
        val result = repository.getToBeFixedProcedures(jobId)
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
    val currentCapturedImage = currentStep.capturedImage

    if (isCameraActive) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            CameraPreview(
                imageCapture = imageCapture,
                resolutionSelector = resolutionSelector,
                modifier = Modifier.fillMaxSize()
            )

            if (currentCapturedImage != null) {
                AsyncImage(
                    model = currentCapturedImage,
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .border(2.dp, if (currentCapturedImage != null) AccentGreen else AccentBlue, RoundedCornerShape(8.dp))
            )

            if (currentCapturedImage != null) {
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
                    label = "PREVIOUS STEP",
                    onClick = { isCameraActive = false },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                if (currentCapturedImage != null) {
                    RealWearButton(
                        label = "RETAKE",
                        onClick = {
                            steps = steps.mapIndexed { i, s ->
                                if (i == currentStepIndex) s.copy(capturedImage = null) else s
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
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
                                    steps = steps.mapIndexed { i, s ->
                                        if (i == currentStepIndex) s.copy(capturedImage = file) else s
                                    }
                                },
                                onError = { exception ->
                                    Log.e("ToBeFixedCamera", "Capture failed", exception)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                RealWearButton(
                    label = "VERIFY CAPTURE",
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                            isCameraActive = false
                        } else {
                            onComplete()
                        }
                    },
                    enabled = currentCapturedImage != null,
                    containerColor = AccentGreen,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
            ) {
                AsyncImage(
                    model = currentStep.stepImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(BgSurface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RealWearButton(
                    label = "PREVIOUS STEP",
                    onClick = {
                        if (currentStepIndex > 0) {
                            currentStepIndex--
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RealWearButton(
                    label = "DEFECTS FIXED",
                    onClick = { isCameraActive = true },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
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

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun ToBeFixedScreenPreview() {
    QAApp_RealwearTheme {
        ToBeFixedScreen(
            jobId = "PROG-1",
            onComplete = {},
            onBack = {}
        )
    }
}
