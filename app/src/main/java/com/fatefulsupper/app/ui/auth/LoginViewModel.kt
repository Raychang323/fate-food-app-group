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
import com.fatefulsupper.app.api.ApiClient
import com.fatefulsupper.app.api.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Event wrapper for one-time consumable events


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


    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }

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
                    // 嘗試解析 body
                    val result = response.body()
                    if (response.isSuccessful && result != null) {
                        if (result["status"] == "ok") {
                            val username = result["username"]?.toString() ?: ""
                            _snackbarMessage.value = Event("歡迎 $username")
                            _loginResult.value = true
                        } else {
                            // 後端回傳 JSON，但 status = error
                            _loginError.value = result["message"]?.toString() ?: "登入失敗"
                            _loginResult.value = false
                        }
                    } else {
                        // 非 2xx，例如 401/403，嘗試解析 errorBody
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            // 可解析 JSON 或直接顯示文字
                            _loginError.value = "帳號或密碼錯誤"
                        } else {
                            _loginError.value = "帳號或密碼錯誤或伺服器回應錯誤"
                        }
                        _loginResult.value = false
                    }
                } catch (e: Exception) {
                    // 解析過程出錯
                    _loginError.value = "登入失敗: ${e.localizedMessage}"
                    _loginResult.value = false
                }
            }

            override fun onFailure(
                call: Call<Map<String, Any>?>,
                t: Throwable
            ) {
                _loginResult.value = false // Reset result to prevent re-navigation on config change
                _loginError.value = null            }

        })
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
