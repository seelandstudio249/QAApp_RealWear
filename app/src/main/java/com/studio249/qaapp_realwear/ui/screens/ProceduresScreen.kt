package com.studio249.qaapp_realwear.ui.screens

import android.content.Context
import android.util.Log
import android.util.Rational
import android.view.Surface
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview as ComposablePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Step
import com.studio249.qaapp_realwear.model.StepStatus
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.*
import com.studio249.qaapp_realwear.utils.StorageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class ProcedurePattern {
    Steps, Capture, Review
}

@Composable
fun ProceduresScreen(
    jobId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SeedDataRepository() }
    
    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var furthestStepIndex by remember { mutableIntStateOf(0) }
    var currentPattern by remember { mutableStateOf(ProcedurePattern.Steps) }
    var isLoading by remember { mutableStateOf(true) }
    var detectList by remember { mutableStateOf<List<String>>(emptyList()) }

    // State for captured image in current session
    var tempCapturedImage by remember { mutableStateOf<File?>(null) }
    
    // Define a consistent ResolutionSelector to ensure full 4:3 native sensor FOV matches between Preview and Capture
    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
    }

    // CameraX setups using the consistent resolution selector
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
            Text("No procedures found", color = TextPrimary)
            RealWearButton(label = "BACK", onClick = onBack)
        }
        return
    }

    val currentStep = steps[currentStepIndex]

    // Added statusBarsPadding to prevent UI from hiding under the system bar
    Box(modifier = Modifier.fillMaxSize().background(BgPrimary).statusBarsPadding()) {
        if (currentPattern == ProcedurePattern.Capture) {
            // Full-Screen Camera Preview Feed
            CameraPreview(
                imageCapture = imageCapture,
                resolutionSelector = resolutionSelector,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay captured photo across full screen once captured
            if (tempCapturedImage != null) {
                AsyncImage(
                    model = tempCapturedImage,
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Square Viewfinder Overlay - Centered in the screen area
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .border(2.dp, if (tempCapturedImage != null) AccentGreen else AccentBlue, RoundedCornerShape(8.dp))
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

            // Floating Top Bar Overlay (Translucent during live camera preview)
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

            // Floating Bottom Bar Overlay (Translucent during live camera preview)
            RealWearBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                backgroundColor = Color.Black.copy(alpha = 0.40f)
            ) {
                RealWearButton(
                    label = "BACK",
                    onClick = { currentPattern = ProcedurePattern.Steps },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                if (tempCapturedImage != null) {
                    RealWearButton(
                        label = "RETAKE",
                        onClick = { tempCapturedImage = null },
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
                                    tempCapturedImage = file
                                },
                                onError = {
                                    Log.e("Camera", "Capture failed", it)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                RealWearButton(
                    label = "VERIFY CAPTURE",
                    onClick = {
                        currentPattern = ProcedurePattern.Review
                    },
                    enabled = tempCapturedImage != null,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            // Standard Layout for Steps and Review patterns
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
                        ProcedurePattern.Steps -> {
                            AsyncImage(
                                model = currentStep.stepImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        ProcedurePattern.Review -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Left 70%: Captured Image
                                Box(modifier = Modifier.weight(0.7f).fillMaxHeight().background(BgSurface)) {
                                    if (tempCapturedImage != null) {
                                        AsyncImage(
                                            model = tempCapturedImage,
                                            contentDescription = "Captured Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text("NO IMAGE CAPTURED", modifier = Modifier.align(Alignment.Center), color = TextSecondary)
                                    }

                                    RealWearButton(
                                        label = "RETAKE",
                                        onClick = {
                                            tempCapturedImage = null
                                            currentPattern = ProcedurePattern.Capture
                                        },
                                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                        containerColor = Color.Black.copy(alpha = 0.5f),
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
                        else -> {}
                    }
                }

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
                        ProcedurePattern.Review -> {
                            RealWearButton(
                                label = "BACK TO STEPS",
                                onClick = {
                                    currentPattern = ProcedurePattern.Steps
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
                                        tempCapturedImage = null
                                    }
                                },
                                containerColor = if (isLastStep) AccentGreen else BgSurfaceRaised,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    resolutionSelector: ResolutionSelector,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // FILL_CENTER fills the entire screen area under the transparent top and bottom bars
                scaleType = PreviewView.ScaleType.FILL_CENTER
                // COMPATIBLE mode (TextureView) handles Compose Z-order better for overlays
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            // Use the same resolution selector for the preview to match aspect ratios
            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    // Unbind all before binding to ensure new settings apply correctly
                    cameraProvider.unbindAll()

                    // Use ViewPort & UseCaseGroup so CameraX forces Preview and ImageCapture to have identical cropping and FOV
                    val viewPort = previewView.viewPort ?: ViewPort.Builder(
                        Rational(16, 9),
                        previewView.display?.rotation ?: Surface.ROTATION_0
                    ).setScaleType(ViewPort.FILL_CENTER).build()

                    val useCaseGroup = UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageCapture)
                        .setViewPort(viewPort)
                        .build()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        useCaseGroup
                    )
                    // Reset hardware zoom to 1.0x to ensure native wide FOV
                    camera.cameraControl.setZoomRatio(1.0f)
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = modifier
    )
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
fun ProceduresScreenPreview() {
    QAApp_RealwearTheme {
        ProceduresScreen(
            jobId = "OUT-1",
            onComplete = {},
            onBack = {}
        )
    }
}
