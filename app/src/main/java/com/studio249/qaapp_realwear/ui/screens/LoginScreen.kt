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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

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
            delay(400)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        RealWearTopBar(title = "LOGIN")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.76f),
            contentAlignment = Alignment.Center
        ) {
            // Camera Feed fills the entire content area
            if (loginState == LoginState.Scanning) {
                QrScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = { qrContent ->
                        if (loginState == LoginState.Scanning) {
                            loginState = LoginState.LoggingIn
                        }
                    }
                )

                // Visual Scanning Frame Overlay (Center Reticle)
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .border(2.dp, AccentBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                    .background(Color.Black.copy(alpha = 0.4f))
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
                    delay(1500)
                    loginState = LoginState.Complete
                }
                LoginState.Complete -> {
                    delay(800) 
                    currentOnLoginSuccess()
                }
                else -> {}
            }
        }

        // Bottom area matching the weight of other screens
        Box(modifier = Modifier.fillMaxWidth().weight(0.14f))
    }
}

@Composable
fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
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
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
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
