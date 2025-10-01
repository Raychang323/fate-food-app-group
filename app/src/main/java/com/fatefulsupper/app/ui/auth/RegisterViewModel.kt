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
import com.fatefulsupper.app.api.ApiClient
import com.fatefulsupper.app.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

class RegisterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }

    fun register(userid: String, password: String, email: String, username: String, role: String) {

        // --- 欄位檢查 ---
        when {
            userid.isBlank() -> { _registrationError.value = "請輸入帳號"; return }
            password.isBlank() -> { _registrationError.value = "請輸入密碼"; return }
            password.length < 6 -> { _registrationError.value = "密碼至少6個字元"; return }
            email.isBlank() -> { _registrationError.value = "請輸入 Email"; return }
            username.isBlank() -> { _registrationError.value = "請輸入使用者名稱"; return }
        }

        _isLoading.value = true
        _registrationError.value = null

        // --- Retrofit 呼叫 ---
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
