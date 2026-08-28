package com.studio249.qaapp_realwear.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object JobHub : Screen("job_hub")
    
    object JobList : Screen("job_list/{type}") {
        fun createRoute(type: String) = "job_list/$type"
    }

    object NewInspections : Screen("new_inspections/{jobId}") {
        fun createRoute(jobId: String) = "new_inspections/$jobId"
    }

    object ToBeFixed : Screen("to_be_fixed/{jobId}") {
        fun createRoute(jobId: String) = "to_be_fixed/$jobId"
    }

    object Reinspection : Screen("reinspection/{jobId}") {
        fun createRoute(jobId: String) = "reinspection/$jobId"
    }

    object TestCam : Screen("test_cam")
    object TestResult : Screen("test_result")
}
