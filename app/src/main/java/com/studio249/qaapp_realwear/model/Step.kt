package com.studio249.qaapp_realwear.model

import java.io.File

enum class StepStatus {
    Pending, Captured, Verified
}

data class Step(
    val index: Int,
    val title: String,
    val stepImage: String,           // for Steps Pattern
    val status: StepStatus = StepStatus.Pending,
    val capturedImage: File? = null,
    val verifyResponse: String? = null,        // cached PostVerifyImage result
    val selectedDetectItem: String? = null // user's Detect List choice, if any
)
