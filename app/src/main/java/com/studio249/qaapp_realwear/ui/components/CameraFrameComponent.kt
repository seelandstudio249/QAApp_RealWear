package com.studio249.qaapp_realwear.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme

/**
 * A reusable scanning frame component with corner brackets and a moving scan line animation.
 */
@Composable
fun CameraFrameComponent(
    modifier: Modifier = Modifier,
    frameColor: Color = AccentBlue,
    strokeWidth: Dp = 3.dp,
    cornerLength: Dp = 40.dp,
    animationDurationMillis: Int = 2000,
    showAnimation: Boolean = true
) {
    Box(
        modifier = modifier
            .drawBehind {
                val strokeWidthPx = strokeWidth.toPx()
                val cornerLengthPx = cornerLength.toPx()

                // Top Left
                drawLine(color = frameColor, start = Offset(0f, 0f), end = Offset(cornerLengthPx, 0f), strokeWidth = strokeWidthPx)
                drawLine(color = frameColor, start = Offset(0f, 0f), end = Offset(0f, cornerLengthPx), strokeWidth = strokeWidthPx)

                // Top Right
                drawLine(color = frameColor, start = Offset(size.width, 0f), end = Offset(size.width - cornerLengthPx, 0f), strokeWidth = strokeWidthPx)
                drawLine(color = frameColor, start = Offset(size.width, 0f), end = Offset(size.width, cornerLengthPx), strokeWidth = strokeWidthPx)

                // Bottom Left
                drawLine(color = frameColor, start = Offset(0f, size.height), end = Offset(cornerLengthPx, size.height), strokeWidth = strokeWidthPx)
                drawLine(color = frameColor, start = Offset(0f, size.height), end = Offset(0f, size.height - cornerLengthPx), strokeWidth = strokeWidthPx)

                // Bottom Right
                drawLine(color = frameColor, start = Offset(size.width, size.height), end = Offset(size.width - cornerLengthPx, size.height), strokeWidth = strokeWidthPx)
                drawLine(color = frameColor, start = Offset(size.width, size.height), end = Offset(size.width, size.height - cornerLengthPx), strokeWidth = strokeWidthPx)
            }
    ) {
        if (showAnimation) {
            val infiniteTransition = rememberInfiniteTransition(label = "scanning")
            val scanPosition by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animationDurationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scanLine"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.02f)
                    .align(BiasAlignment(0f, (scanPosition * 2) - 1f))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                frameColor,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 300)
@Composable
fun CameraFrameComponentPreview() {
    QAApp_RealwearTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CameraFrameComponent(
                modifier = Modifier
                    .size(200.dp)
            )
        }
    }
}
