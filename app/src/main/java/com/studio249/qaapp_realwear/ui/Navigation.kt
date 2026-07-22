package com.studio249.qaapp_realwear.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object TaskList : Screen("task_list")
    object TaskHub : Screen("task_hub/{taskId}") {
        fun createRoute(taskId: String) = "task_hub/$taskId"
    }
    object Capture : Screen("capture/{taskId}") {
        fun createRoute(taskId: String) = "capture/$taskId"
    }
    object Review : Screen("review/{taskId}") {
        fun createRoute(taskId: String) = "review/$taskId"
    }
}
