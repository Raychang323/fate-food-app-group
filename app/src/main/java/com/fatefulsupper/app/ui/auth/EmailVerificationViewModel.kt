package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {
import com.fatefulsupper.app.data.model.ApiResponse
import com.fatefulsupper.app.api.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EmailVerificationViewModel(private val userid: String) : ViewModel() {

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
    private val COOLDOWN_TIME_SECONDS = 300 //冷卻時間
    private var cooldownJob: kotlinx.coroutines.Job? = null

    private val api = RetrofitClient.apiService

    // 驗證碼確認
    fun verifyCode(code: String) {
        _isLoading.value = true
        _verificationError.value = null

        viewModelScope.launch {
            try {
                val response = api.verifyEmailCode(userid, code)
                if (response.isSuccessful) {
                    val body: ApiResponse? = response.body()
                    if (body?.status == "ok") {
                        _verificationResult.value = true
                    } else {
                        _verificationError.value = body?.message ?: "驗證失敗"
                        _verificationResult.value = false
                    }
                } else {
                    _verificationError.value = "驗證失敗，HTTP ${response.code()}"
                    _verificationResult.value = false
                }
            } catch (e: IOException) {
                _verificationError.value = "網路錯誤，請稍後再試"
                _verificationResult.value = false
            } catch (e: HttpException) {
                _verificationError.value = "伺服器錯誤，請稍後再試"
                _verificationResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 重新寄送驗證碼
    fun resendVerificationCode() {
        if (_resendCooldownActive.value == true) return

        _resendCooldownActive.value = true
        startCooldown()

        viewModelScope.launch {
            try {
                val response = api.resendEmailCode(userid)
                val body: ApiResponse? = response.body()
                if (response.isSuccessful && body != null) {
                    if (body.status == "ok") {
                        _verificationError.value = "驗證碼已重新寄送"
                    } else {
                        _verificationError.value = body.message ?: "重新寄送失敗"
                    }
                } else {
                    _verificationError.value = "HTTP ${response.code()} 重新寄送失敗"
                }
            } catch (e: Exception) {
                _verificationError.value = "網路或伺服器異常：${e.localizedMessage}"
            }
        }
    }

    // 冷卻計時
    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in COOLDOWN_TIME_SECONDS downTo 1) {
                _resendCooldownSeconds.value = i
                kotlinx.coroutines.delay(1000)
            }
            _resendCooldownActive.value = false
            _resendCooldownSeconds.value = 0
        }
    }

    fun onAttemptComplete() {
        _verificationResult.value = false // Reset result
    // 重置狀態
    fun onAttemptComplete() {
        _verificationResult.value = false
        _verificationError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel() // Ensure coroutine is cancelled when ViewModel is cleared
    }
}
        cooldownJob?.cancel()
    }
}

