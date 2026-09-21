package com.studio249.qaapp_realwear.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("fullname") val fullname: String,
    @SerializedName("usergroups") val usergroups: List<String>,
    @SerializedName("auth_token") val authToken: String
)
