package com.studio249.qaapp_realwear.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object StorageUtils {
    private const val APP_FOLDER = "QAApp"

    fun getTaskFolder(context: Context, taskId: String): File {
        // Moving storage "above" Documents to the root of Internal Storage
        // Path: /storage/emulated/0/QAApp/Task/{TaskID}/
        val root = Environment.getExternalStorageDirectory()
        val appDir = File(root, APP_FOLDER)
        val taskDir = File(appDir, "Task/$taskId")
        
        if (!taskDir.exists()) {
            // Note: Creating directories at the root requires broad storage permissions 
            // or specific device configurations on Android 13+.
            taskDir.mkdirs()
        }
        return taskDir
    }

    fun generateFileName(taskId: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${taskId}_$timestamp.jpg"
    }
    
    fun getOutputOptions(context: Context, taskId: String): File {
        return File(getTaskFolder(context, taskId), generateFileName(taskId))
    }
}
