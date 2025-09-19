package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _verificationResult = MutableLiveData<Boolean>()
    val verificationResult: LiveData<Boolean> = _verificationResult

    private val _verificationError = MutableLiveData<String?>()
    val verificationError: LiveData<String?> = _verificationError

    private val _resendCooldownActive = MutableLiveData(false)
    val resendCooldownActive: LiveData<Boolean> = _resendCooldownActive

    private val _resendCooldownSeconds = MutableLiveData(0)
    val resendCooldownSeconds: LiveData<Int> = _resendCooldownSeconds

    private var cooldownJob: Job? = null
    private val COOLDOWN_TIME_SECONDS = 60

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _verificationError.value = null
            delay(1000) // Simulate network delay

            if (code == "123456") { // Dummy verification code
                _verificationResult.value = true
            } else {
                _verificationError.value = "驗證碼錯誤或已過期"
                _verificationResult.value = false
            }
            _isLoading.value = false
        }
    }

    fun resendVerificationCode() {
        if (_resendCooldownActive.value == true) return

        viewModelScope.launch {
            // TODO: Call repository to resend code
            // For now, just simulate and start cooldown
            startCooldown()
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel() // Cancel any existing cooldown
        cooldownJob = viewModelScope.launch {
            _resendCooldownActive.value = true
            for (i in COOLDOWN_TIME_SECONDS downTo 1) {
                _resendCooldownSeconds.value = i
                delay(1000)
            }
            _resendCooldownActive.value = false
            _resendCooldownSeconds.value = 0
        }
    }

    fun onAttemptComplete() {
        _verificationResult.value = false // Reset result
        _verificationError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel() // Ensure coroutine is cancelled when ViewModel is cleared
    }
}
