package com.studio249.qaapp_realwear.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object JobHub : Screen("job_hub")
    
    object JobList : Screen("job_list/{type}") {
        fun createRoute(type: String) = "job_list/$type"
    }

    object Procedures : Screen("procedures/{jobId}") {
        fun createRoute(jobId: String) = "procedures/$jobId"
    }

    object InProgress : Screen("in_progress/{jobId}") {
        fun createRoute(jobId: String) = "in_progress/$jobId"
    }

    object InVerify : Screen("in_verify/{jobId}") {
        fun createRoute(jobId: String) = "in_verify/$jobId"
    }

    object TestCam : Screen("test_cam")
    object TestResult : Screen("test_result")
}
