package com.studio249.qaapp_realwear.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.studio249.qaapp_realwear.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qa_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "session_token"
        private const val KEY_USER = "user_profile"
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun setToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null)
        return if (json != null) gson.fromJson(json, User::class.java) else null
    }

    fun setUser(user: User) {
        prefs.edit {
            putString(KEY_USER, gson.toJson(user))
            putString(KEY_TOKEN, user.authToken)
        }
    }

    fun clear() {
        prefs.edit {
            remove(KEY_USER)
            remove(KEY_TOKEN)
        }
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }
}
