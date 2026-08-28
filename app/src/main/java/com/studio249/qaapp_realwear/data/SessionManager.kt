package com.studio249.qaapp_realwear.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qa_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "session_token"
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun setToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }
}
