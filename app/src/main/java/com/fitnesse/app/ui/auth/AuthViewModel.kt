package com.fitnesse.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.firebase.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val resetSent: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _state.value = _state.value.copy(isSuccess = auth.currentUser != null)
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun toggleMode() {
        _state.value = _state.value.copy(isLogin = !_state.value.isLogin, error = null)
    }

    fun sendPasswordReset() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "Enter your email first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.sendPasswordReset(email)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false, resetSent = true, error = null)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to send reset email")
                },
            )
        }
    }

    fun submit() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = if (s.isLogin) {
                authRepository.signIn(s.email.trim(), s.password)
            } else {
                authRepository.signUp(s.email.trim(), s.password)
            }
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Authentication failed")
                },
            )
        }
    }
}
