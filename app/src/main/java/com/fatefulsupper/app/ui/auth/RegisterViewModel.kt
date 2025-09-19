package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // True if registration (initial step) is successful, leading to e.g., email verification
    private val _registrationStepResult = MutableLiveData<Boolean>()
    val registrationStepResult: LiveData<Boolean> = _registrationStepResult

    private val _registrationError = MutableLiveData<String?>()
    val registrationError: LiveData<String?> = _registrationError

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _registrationError.value = null // Clear previous error

            // Simulate network delay
            delay(1500)

            if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                _registrationError.value = "所有欄位皆為必填"
                _registrationStepResult.value = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _registrationError.value = "請輸入有效的 Email 地址"
                _registrationStepResult.value = false
            } else if (password != confirmPassword) {
                _registrationError.value = "密碼與確認密碼不相符"
                _registrationStepResult.value = false
            } else if (password.length < 6) {
                _registrationError.value = "密碼長度至少需6位"
                _registrationStepResult.value = false
            } else {
                // Simulate successful registration call to backend/service
                // In a real app, you would interact with a UserRepository here.
                _registrationStepResult.value = true
            }
            _isLoading.value = false
        }
    }

    // Call this method when navigation away from registration occurs or error is handled
    fun onRegistrationAttemptComplete() {
        _registrationStepResult.value = false // Reset result
        _registrationError.value = null
    }
}
