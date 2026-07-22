package com.studio249.qaapp_realwear.model

data class User(
    val id: String,
    val name: String,
    val avatarRes: Int? = null // Using resource ID for simple avatar as requested
)
