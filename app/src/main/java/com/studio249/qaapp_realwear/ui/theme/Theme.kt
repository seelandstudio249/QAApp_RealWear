package com.studio249.qaapp_realwear.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HighContrastColorScheme = darkColorScheme(
    primary = RealWearYellow,
    onPrimary = RealWearBlack,
    secondary = RealWearWhite,
    onSecondary = RealWearBlack,
    background = RealWearBlack,
    surface = RealWearBlack,
    onBackground = RealWearWhite,
    onSurface = RealWearWhite,
    error = RealWearRed,
    onError = RealWearWhite
)

@Composable
fun QAApp_RealwearTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = RealWearBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = HighContrastColorScheme,
        typography = Typography,
        content = content
    )
}
