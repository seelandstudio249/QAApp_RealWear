package com.studio249.qaapp_realwear.ui.screens

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.studio249.qaapp_realwear.ui.components.CameraFrameComponent
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.BgPrimary
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

enum class LoginState {
    Scanning, LoggingIn, Complete, Failed
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var loginState by remember { mutableStateOf(LoginState.Scanning) }
    var dots by remember { mutableStateOf("") }
    
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(loginState) {
        while (loginState == LoginState.Scanning || loginState == LoginState.LoggingIn) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            delay(400.milliseconds)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgPrimary).statusBarsPadding()) {
        // Top Bar - Strictly confined at top with elevation
        Surface(shadowElevation = 4.dp, color = BgPrimary) {
            RealWearTopBar(title = "LOGIN")
        }

        // Camera Content Area - Strictly confined below top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            // Camera Feed fills content area without bleeding past top bar
            if (loginState == LoginState.Scanning) {
                QrScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = { qrContent ->
                        if (loginState == LoginState.Scanning) {
                            loginState = LoginState.LoggingIn
                        }
                    }
                )

                // Visual Scanning Frame Overlay
                CameraFrameComponent(
                    modifier = Modifier
                        .fillMaxHeight(0.75f)
                        .aspectRatio(1f)
                )
            } else if (loginState == LoginState.LoggingIn) {
                Text(
                    text = "AUTHENTICATING...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AccentBlue
                )
            }

            // Status Text and Buttons Overlay (at the bottom of the camera area)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    //.background(Color.Black.copy(alpha = 0.4f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val statusText = when (loginState) {
                    LoginState.Scanning -> "SCAN QR CODE$dots"
                    LoginState.LoggingIn -> "LOGGING IN$dots"
                    LoginState.Complete -> "LOGIN COMPLETE"
                    LoginState.Failed -> "LOGIN FAILED"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )

                if (loginState == LoginState.Scanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    RealWearButton(
                        label = "SIMULATE SCAN",
                        onClick = { loginState = LoginState.LoggingIn }
                    )
                }
            }
        }

        LaunchedEffect(loginState) {
            when (loginState) {
                LoginState.LoggingIn -> {
                    delay(1500.milliseconds)
                    loginState = LoginState.Complete
                }
                LoginState.Complete -> {
                    delay(800.milliseconds)
                    currentOnLoginSuccess()
                }
                else -> {}
            }
        }
    }
}

@Composable
fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val resolutionSelector = remember {
        androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
            .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FIT_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    QrCodeAnalyzer { qrCode ->
                        onQrCodeScanned(qrCode)
                    }
                )

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                    // Explicitly set camera hardware zoom to 1.0x (1x full wide view)
                    camera.cameraControl.setZoomRatio(1.0f)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { 
                            onQrCodeScanned(it)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun LoginScreenPreview() {
    QAApp_RealwearTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
