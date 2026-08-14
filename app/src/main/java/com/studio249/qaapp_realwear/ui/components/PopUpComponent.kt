package com.studio249.qaapp_realwear.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RealWearPopup(
    show: Boolean,
    title: String,
    message: String,
    isError: Boolean = false,
    durationMs: Long = 3000,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show, isError, onRetry) {
        if (show && onRetry == null) {
            delay(durationMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary.copy(alpha = 0.7f))
                .clickable(enabled = false) {}, // Scrim
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .heightIn(min = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSurface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        onRetry != null -> Icons.Default.Refresh
                        isError -> Icons.Default.Error
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when {
                        onRetry != null -> AccentBlue
                        isError -> AccentRed
                        else -> AccentGreen
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (isError) AccentRed else TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    RealWearButton(
                        label = "RETRY",
                        onClick = onRetry,
                        containerColor = AccentBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Extension to allow clickable on Box even if we don't want it to do anything (to consume touch)
private fun Modifier.clickable(enabled: Boolean, onClick: () -> Unit) = this.then(
    Modifier.clickable(enabled = enabled, onClick = onClick)
)

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun SuccessPopupPreview() {
    QAApp_RealwearTheme {
        RealWearPopup(
            show = true,
            title = "Success",
            message = "Task completed successfully",
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun ErrorPopupPreview() {
    QAApp_RealwearTheme {
        RealWearPopup(
            show = true,
            title = "Error",
            message = "Failed to upload image. Please check your connection.",
            isError = true,
            onRetry = {},
            onDismiss = {}
        )
    }
}
