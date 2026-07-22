package com.studio249.qaapp_realwear.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object StorageUtils {
    private const val APP_FOLDER = "QAApp"

    fun getTaskFolder(context: Context, taskId: String): File {
        val root = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) 
            ?: File(context.filesDir, Environment.DIRECTORY_PICTURES)
        
        // We use app-specific storage for simplicity and reliability across Android versions,
        // but we can map to public storage if needed.
        val appDir = File(root, APP_FOLDER)
        val taskDir = File(appDir, "Task/$taskId")
        
        if (!taskDir.exists()) {
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
