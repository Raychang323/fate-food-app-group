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

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private val apiService = RetrofitClient.apiService

    fun login(usernameOrEmail: String, password: String) {
        _isLoading.value = true
        _loginError.value = null

        val call = apiService.login(usernameOrEmail, password)

        call.enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                _isLoading.value = false

                try {
                    val result = response.body()
                    if (response.isSuccessful && result != null) {
                        if (result["status"] == "ok") {
                            val username = result["username"]?.toString() ?: ""
                            _snackbarMessage.value = Event("歡迎 $username")
                            _loginResult.value = true
                        } else {
                            _loginError.value = result["message"]?.toString() ?: "登入失敗"
                            _loginResult.value = false
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            _loginError.value = "帳號或密碼錯誤"
                        } else {
                            _loginError.value = "帳號或密碼錯誤或伺服器回應錯誤"
                        }
                        _loginResult.value = false
                    }
                } catch (e: Exception) {
                    _loginError.value = "登入失敗: ${e.localizedMessage}"
                    _loginResult.value = false
                }
            }

            override fun onFailure(call: Call<Map<String, Any>?>, t: Throwable) {
                _isLoading.value = false
                _loginError.value = "網路連線失敗，請稍後再試"
                _loginResult.value = false
            }
        })
    }

    fun onLoginAttemptComplete() {
        _loginResult.value = false
        _loginError.value = null
    }

    fun onNavigatedFromRegistrationSuccess() {
        _snackbarMessage.value = Event("註冊成功！請登入。")
    }
}