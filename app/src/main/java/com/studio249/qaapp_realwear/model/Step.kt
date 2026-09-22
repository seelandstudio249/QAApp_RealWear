package com.studio249.qaapp_realwear.model

import java.io.File

enum class StepStatus {
    Pending, Captured, Verified
}

data class BoxCoordinates(
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double
)

data class Detection(
    val label: String,
    val originalLabel: String,
    val box: BoxCoordinates? = null,
    val score: Double = 1.0
)

data class Step(
    val index: Int,
    val title: String,
    val stepImage: String,           // for Steps Pattern
    val status: StepStatus = StepStatus.Pending,
    val capturedImage: File? = null,
    val verifyResponse: String? = null,        // cached PostVerifyImage result
    val detections: List<Detection> = emptyList()
)
