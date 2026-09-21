package com.studio249.qaapp_realwear.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studio249.qaapp_realwear.data.Repository
import com.studio249.qaapp_realwear.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginState {
    Scanning, LoggingIn, Complete, Failed
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Scanning)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onLogin(qrToken: String) {
        if (_loginState.value != LoginState.Scanning) return

        _loginState.value = LoginState.LoggingIn
        _errorMessage.value = null

        viewModelScope.launch {
            repository.postLogin(qrToken).fold(
                onSuccess = { user ->
                    sessionManager.setUser(user)
                    _loginState.value = LoginState.Complete
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Failed
                    _errorMessage.value = error.message ?: "Unknown error"
                }
            )
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Scanning
        _errorMessage.value = null
    }
}
