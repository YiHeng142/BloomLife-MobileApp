package com.example.bloomlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloomlife.data.ProfileRepository
import com.example.bloomlife.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val userId: String   // Supabase Auth user ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfile(userId = userId))
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(repository.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        loadProfile()
    }

    fun toggleDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
        _isDarkMode.value = enabled
    }

    fun updateUsername(name: String) {
        _uiState.update { it.copy(username = name) }
    }

    fun updateBirthDate(timestamp: Long) {
        val calculatedAge = calculateAge(timestamp)
        _uiState.update {
            it.copy(
                birthDate = timestamp,
                age = if (calculatedAge >= 0) calculatedAge.toString() else "0"
            )
        }
    }

    fun updateGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateProfileHeight(height: String) {
        val filtered = height.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(height = filtered) }
        autoCalculateProfileBmi()
    }

    fun updateProfileWeight(weight: String) {
        val filtered = weight.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(weight = filtered) }
        autoCalculateProfileBmi()
    }

    private fun autoCalculateProfileBmi() {
        val hCm = _uiState.value.height.toDoubleOrNull() ?: 0.0
        val w = _uiState.value.weight.toDoubleOrNull() ?: 0.0
        val hM = hCm / 100.0
        if (hM > 0 && w > 0) {
            val bmiValue = w / (hM * hM)
            _uiState.update {
                it.copy(
                    bmi = String.format(Locale.US, "%.2f", bmiValue),
                    category = getBmiCategory(bmiValue)
                )
            }
        } else {
            _uiState.update { it.copy(bmi = "", category = "") }
        }
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    private fun calculateAge(birthTimestamp: Long): Int {
        val birthCalendar = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    // --- Standalone BMI Calculator ---
    private val _calcHeight = MutableStateFlow("")
    val calcHeight: StateFlow<String> = _calcHeight.asStateFlow()
    private val _calcWeight = MutableStateFlow("")
    val calcWeight: StateFlow<String> = _calcWeight.asStateFlow()
    private val _calcResultBmi = MutableStateFlow("")
    val calcResultBmi: StateFlow<String> = _calcResultBmi.asStateFlow()
    private val _calcResultCategory = MutableStateFlow("")
    val calcResultCategory: StateFlow<String> = _calcResultCategory.asStateFlow()

    fun updateCalcHeight(height: String) {
        _calcHeight.value = height.filter { it.isDigit() || it == '.' }
        runCalculator()
    }

    fun updateCalcWeight(weight: String) {
        _calcWeight.value = weight.filter { it.isDigit() || it == '.' }
        runCalculator()
    }

    private fun runCalculator() {
        val hCm = _calcHeight.value.toDoubleOrNull() ?: 0.0
        val w = _calcWeight.value.toDoubleOrNull() ?: 0.0
        val hM = hCm / 100.0
        if (hM > 0 && w > 0) {
            val bmiValue = w / (hM * hM)
            _calcResultBmi.value = String.format(Locale.US, "%.2f", bmiValue)
            _calcResultCategory.value = getBmiCategory(bmiValue)
        } else {
            _calcResultBmi.value = ""
            _calcResultCategory.value = ""
        }
    }

    // --- Account Actions ---
    fun updateCredentials(newUsername: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateUsername(userId, newUsername)
            loadProfile()
            onSuccess()
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            repository.saveProfile(_uiState.value)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = repository.loadProfile(userId)
            if (profile != null) {
                _uiState.value = profile
            }
        }
    }
}