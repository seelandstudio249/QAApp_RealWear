package com.studio249.qaapp_realwear.utils

object NetworkConfig {
    const val HOST_IP = "192.168.1.106:3000" // Default, user must change this
    const val BASE_PATH = "/inspection/realwear"
    const val BASE_URL = "http://$HOST_IP$BASE_PATH/"
}
