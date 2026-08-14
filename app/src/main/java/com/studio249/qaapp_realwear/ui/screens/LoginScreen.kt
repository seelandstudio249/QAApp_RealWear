package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import kotlinx.coroutines.delay

enum class LoginState {
    Scanning, LoggingIn, Complete, Failed
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var loginState by remember { mutableStateOf(LoginState.Scanning) }
    var dots by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    // Crucial: Keep a stable reference to the callback
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    // Handle the scanning dots animation
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

    Column(modifier = Modifier.fillMaxSize()) {
        RealWearTopBar(title = "LOGIN")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.76f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Square camera frame simulation
                Box(
                    modifier = Modifier
                        .size(200.dp) 
                        .border(2.dp, AccentBlue, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CAMERA FEED",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                val statusText = when (loginState) {
                    LoginState.Scanning -> "SCANNING$dots"
                    LoginState.LoggingIn -> "LOGGING IN$dots"
                    LoginState.Complete -> "LOGIN COMPLETE"
                    LoginState.Failed -> "LOGIN FAILED"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.displayLarge
                )
                
                // HIDE THE BUTTON once scanning/logging in begins
                if (loginState == LoginState.Scanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    RealWearButton(
                        label = "SIMULATE SCAN",
                        onClick = {
                            loginState = LoginState.LoggingIn
                        }
                    )
                }
            }
        }

        // Handle the login logic simulation flow
        LaunchedEffect(loginState) {
            when (loginState) {
                LoginState.LoggingIn -> {
                    delay(1500)
                    loginState = LoginState.Complete
                }
                LoginState.Complete -> {
                    // Navigate immediately once the state becomes Complete
                    // (The restart caused by 'loginState = Complete' brings us here)
                    delay(800) 
                    currentOnLoginSuccess()
                }
                else -> {}
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(0.14f))
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun LoginScreenPreview() {
    QAApp_RealwearTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
