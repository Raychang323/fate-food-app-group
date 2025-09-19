package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Event wrapper for one-time consumable events
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set // Allow external read but not write

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    @Suppress("unused")
    fun peekContent(): T = content
}

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null // Clear previous error

            // Simulate network delay
            delay(1500)

            // Dummy validation logic
            if (usernameOrEmail.isBlank() || password.isBlank()){
                _loginError.value = "使用者名稱和密碼不可為空"
                _loginResult.value = false
            } else if (usernameOrEmail == "test@example.com" && password == "password") {
                _snackbarMessage.value = Event("登入成功！") // Show Snackbar on successful login
                _loginResult.value = true
            } else {
                _loginError.value = "無效的使用者名稱或密碼"
                _loginResult.value = false
            }
            _isLoading.value = false
        }
    }

    // Call this method when navigation away from login occurs or error is handled
    fun onLoginAttemptComplete() {
        _loginResult.value = false // Reset result to prevent re-navigation on config change
        _loginError.value = null
    }

    // Called by LoginFragment when it receives the argument from successful registration
    fun onNavigatedFromRegistrationSuccess() {
        _snackbarMessage.value = Event("註冊成功！請登入。")
    }
}
