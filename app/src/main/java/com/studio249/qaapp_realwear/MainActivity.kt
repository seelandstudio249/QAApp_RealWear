package com.studio249.qaapp_realwear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studio249.qaapp_realwear.ui.Screen
import com.studio249.qaapp_realwear.ui.screens.CaptureScreen
import com.studio249.qaapp_realwear.ui.screens.LoginScreen
import com.studio249.qaapp_realwear.ui.screens.ReviewScreen
import com.studio249.qaapp_realwear.ui.screens.TaskHubScreen
import com.studio249.qaapp_realwear.ui.screens.TaskListScreen
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QAApp_RealwearTheme {
                MainNavigation()
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
                LoginScreen(onUserSelected = { userId ->
                    navController.navigate(Screen.TaskList.route)
                })
            }

            composable(Screen.TaskList.route) {
                TaskListScreen(onTaskSelected = { taskId ->
                    navController.navigate(Screen.TaskHub.createRoute(taskId))
                })
            }

            composable(
                route = Screen.TaskHub.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                TaskHubScreen(
                    taskId = taskId,
                    onCaptureClick = {
                        navController.navigate(Screen.Capture.createRoute(taskId))
                    },
                    onReviewClick = {
                        navController.navigate(Screen.Review.createRoute(taskId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Capture.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                CaptureScreen(
                    taskId = taskId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Review.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                ReviewScreen(
                    taskId = taskId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
