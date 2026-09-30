package com.studio249.qaapp_realwear.ui.components

import android.util.Log
import android.util.Rational
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

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
