package com.studio249.qaapp_realwear.ui.screens

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.util.Log
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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import com.studio249.qaapp_realwear.utils.StorageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Rational
import android.view.Surface
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop

@Composable
fun TestCamScreen(
    onImageCaptured: (File) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var capturedImage by remember { mutableStateOf<File?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main camera preview or captured photo viewer
        if (capturedImage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = capturedImage,
                    contentDescription = "Captured Photo",
                    modifier = Modifier.aspectRatio(4f / 3f), // Matches the exact box constraint of the preview loop
                    contentScale = ContentScale.Crop          // Eliminates the weird off-center black bounding borders
                )
            }
        } else {
            TestCamPreview(
                imageCapture = imageCapture,
                resolutionSelector = resolutionSelector,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Floating transparent buttons at bottom - No TopBar or BottomBar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CAPTURE transparent button
            TransparentCameraButton(
                label = "CAPTURE",
                enabled = (capturedImage == null && !isCapturing),
                onClick = {
                    if (capturedImage == null && !isCapturing) {
                        isCapturing = true
                        takeTestPhoto(
                            context = context,
                            imageCapture = imageCapture,
                            executor = cameraExecutor,
                            onPhotoTaken = { file ->
                                capturedImage = file
                                isCapturing = false
                                onImageCaptured(file)
                            },
                            onError = {
                                isCapturing = false
                            }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // RETAKE transparent button
            TransparentCameraButton(
                label = "RETAKE",
                enabled = (capturedImage != null),
                onClick = {
                    capturedImage = null
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TransparentCameraButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val alpha = if (enabled) 1.0f else 0.4f
    val borderColor = if (isFocused && enabled) {
        Color(0xFF2196F3) // Highlight border when focused
    } else {
        Color.White.copy(alpha = 0.6f * alpha)
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f * alpha))
            .border(
                width = if (isFocused && enabled) 3.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(enabled = enabled, interactionSource = interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = alpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun TestCamPreview(
    imageCapture: ImageCapture,
    resolutionSelector: ResolutionSelector,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                // 1. Initialize Preview Builder
                val previewBuilder = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)

                // 2. Use Camera2Interop to force turn OFF hardware/software stabilization features
                val camera2Extender = Camera2Interop.Extender(previewBuilder)
                camera2Extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
                )
                camera2Extender.setCaptureRequestOption(
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF
                )

                val preview = previewBuilder.build()
                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                preview.setSurfaceProvider(previewView.surfaceProvider)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()

                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageCapture
                        )
                        camera.cameraControl.setLinearZoom(0.0f)
                    } catch (e: Exception) {
                        Log.e("TestCamScreen", "Camera preview binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.aspectRatio(4f / 3f)
        )
    }
}

private fun takeTestPhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onPhotoTaken: (File) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = StorageUtils.getOutputOptions(context, "TEST_CAM")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("TestCamScreen", "Photo capture failed", exception)
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoTaken(photoFile)
            }
        }
    )
}

@ComposePreview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun TestCamScreenPreview() {
    QAApp_RealwearTheme {
        TestCamScreen()
    }
}
