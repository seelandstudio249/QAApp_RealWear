package com.studio249.qaapp_realwear.data.remote

import com.google.gson.annotations.SerializedName
import com.studio249.qaapp_realwear.model.User

data class LoginRequest(
    @SerializedName("qr_token") val qrToken: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: User
)
