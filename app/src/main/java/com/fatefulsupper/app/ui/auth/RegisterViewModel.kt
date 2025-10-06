package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.util.Event
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registrationStepResult = MutableLiveData<Boolean>()
    val registrationStepResult: LiveData<Boolean> = _registrationStepResult

    private val _registrationError = MutableLiveData<String?>()
    val registrationError: LiveData<String?> = _registrationError

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private val apiService = RetrofitClient.apiService

    fun register(userid: String, password: String, email: String, username: String, role: String) {
        when {
            userid.isBlank() -> { _registrationError.value = "請輸入帳號"; return }
            password.isBlank() -> { _registrationError.value = "請輸入密碼"; return }
            password.length < 6 -> { _registrationError.value = "密碼至少6個字元"; return }
            email.isBlank() -> { _registrationError.value = "請輸入 Email"; return }
            username.isBlank() -> { _registrationError.value = "請輸入使用者名稱"; return }
        }

        _isLoading.value = true
        _registrationError.value = null

        val call = apiService.register(userid, password, email, username, role)
        call.enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result["status"] == "ok") {
                        _registrationStepResult.value = true
                        _snackbarMessage.value = Event("註冊成功！")
                    } else {
                        _registrationStepResult.value = false
                        _registrationError.value = result["message"]?.toString() ?: "註冊失敗"
                    }
                } else {
                    _registrationStepResult.value = false
                    _registrationError.value = "伺服器錯誤"
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                _isLoading.value = false
                _registrationStepResult.value = false
                _registrationError.value = "連線失敗: ${t.localizedMessage}"
            }
        })
    }

    fun onRegistrationAttemptComplete() {
        _registrationStepResult.value = false
        _registrationError.value = null
    }
}