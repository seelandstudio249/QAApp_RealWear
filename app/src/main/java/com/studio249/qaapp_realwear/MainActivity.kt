package com.studio249.qaapp_realwear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studio249.qaapp_realwear.ui.Screen
import com.studio249.qaapp_realwear.ui.screens.JobHubScreen
import com.studio249.qaapp_realwear.ui.screens.JobListScreen
import com.studio249.qaapp_realwear.ui.screens.LoginScreen
import com.studio249.qaapp_realwear.ui.screens.ProceduresScreen
import com.studio249.qaapp_realwear.ui.screens.ToBeFixedScreen
import com.studio249.qaapp_realwear.ui.screens.ReinspectionScreen
import com.studio249.qaapp_realwear.ui.screens.TestCamScreen
import com.studio249.qaapp_realwear.ui.screens.TestResultScreen
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission is required for scanning and capture", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkCameraPermission()

        setContent {
            QAApp_RealwearTheme {
                MainNavigation()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.JobHub.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.JobHub.route) {
                JobHubScreen(
                    onTabSelected = { type ->
                        navController.navigate(Screen.JobList.createRoute(type))
                    }
                )
            }

            composable(
                route = Screen.JobList.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "NewInspections"
                JobListScreen(
                    type = type,
                    onJobSelected = { jobId ->
                        when (type) {
                            "NewInspections" -> navController.navigate(Screen.Procedures.createRoute(jobId))
                            "ToBeFixed" -> navController.navigate(Screen.ToBeFixed.createRoute(jobId))
                            "Reinspection" -> navController.navigate(Screen.Reinspection.createRoute(jobId))
                            "Completed" -> { /* Just view? maybe Procedures too */ }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Procedures.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                ProceduresScreen(
                    jobId = jobId,
                    onComplete = {
                        navController.navigate(Screen.JobHub.route) {
                            popUpTo(Screen.JobHub.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ToBeFixed.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                ToBeFixedScreen(
                    jobId = jobId,
                    onComplete = {
                        navController.navigate(Screen.JobHub.route) {
                            popUpTo(Screen.JobHub.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Reinspection.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                ReinspectionScreen(
                    jobId = jobId,
                    onComplete = {
                        navController.navigate(Screen.JobHub.route) {
                            popUpTo(Screen.JobHub.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TestCam.route) {
                TestCamScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen. TestResult.route) {
                TestResultScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun MainNavigationPreview() {
    QAApp_RealwearTheme {
        MainNavigation()
    }
}
