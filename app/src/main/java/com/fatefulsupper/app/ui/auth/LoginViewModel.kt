package com.fatefulsupper.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.repository.AuthRepository
import com.fatefulsupper.app.util.Event
import com.fatefulsupper.app.util.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Event<String>>()
    val loginSuccess: LiveData<Event<String>> = _loginSuccess

    private val _loginError = MutableLiveData<Event<String>>()
    val loginError: LiveData<Event<String>> = _loginError

    fun login(userid: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = LoginRequest(userid, password)
                val response = authRepository.login(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Login successful, save the token
                        SessionManager.saveAuthToken(getApplication(), authResponse.token)
                        _loginSuccess.postValue(Event("登入成功"))
                    } else {
                        _loginError.postValue(Event("登入失敗"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            val message = json.getString("message")
                            _loginError.postValue(Event("登入失敗: $message"))
                        } catch (e: Exception) {
                            _loginError.postValue(Event("登入失敗: ${response.code()} ${response.message()}"))
                        }
                    } else {
                        _loginError.postValue(Event("登入失敗: ${response.code()} ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                _loginError.postValue(Event("登入失敗: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
