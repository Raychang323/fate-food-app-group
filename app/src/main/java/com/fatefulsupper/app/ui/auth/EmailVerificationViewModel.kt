package com.fatefulsupper.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.ApiResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EmailVerificationViewModel(application: Application, private val userid: String) : AndroidViewModel(application) {

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

    private val api = RetrofitClient.getInstance(application)

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

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in COOLDOWN_TIME_SECONDS downTo 1) {
                _resendCooldownSeconds.value = i
                delay(1000)
            }
            _resendCooldownActive.value = false
            _resendCooldownSeconds.value = 0
        }
    }

    fun onAttemptComplete() {
        _verificationResult.value = false
        _verificationError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
