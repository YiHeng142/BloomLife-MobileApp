package com.example.bloomlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloomlife.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
        _errorMessage.value = null
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _errorMessage.value = null
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
    }

    fun login() {
        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        if (emailValue.isEmpty() || passwordValue.isEmpty()) {
            _errorMessage.value = "Please enter email and password"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    this.email = emailValue
                    this.password = passwordValue
                }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register() {
        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        when {
            emailValue.isEmpty() || passwordValue.isEmpty() -> {
                _errorMessage.value = "Please enter email and password"
                return
            }
            passwordValue.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                return
            }
            passwordValue != _confirmPassword.value -> {
                _errorMessage.value = "Passwords do not match"
                return
            }
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    this.email = emailValue
                    this.password = passwordValue
                }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}