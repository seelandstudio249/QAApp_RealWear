package com.studio249.qaapp_realwear.data.remote

import com.google.gson.annotations.SerializedName

data class RemoteJob(
    @SerializedName("id") val id: String,
    @SerializedName("vehicle_id") val vehicleId: String?,
    @SerializedName("plateno") val vehiclePlateNo: String?,
    @SerializedName("clientname") val clientName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("defect_id") val defectId: String?,
    @SerializedName("defect_type") val defectType: String?,
    @SerializedName("defect_status") val defectStatus: String?
)

data class JobListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RemoteJob>
)
