package com.fatefulsupper.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.data.repository.AuthRepository
import com.fatefulsupper.app.util.Event
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registrationSuccess = MutableLiveData<Event<String>>()
    val registrationSuccess: LiveData<Event<String>> = _registrationSuccess

    private val _registrationError = MutableLiveData<Event<String>>()
    val registrationError: LiveData<Event<String>> = _registrationError

    fun register(email: String, password: String, username: String, userid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = RegisterRequest(email, password, username, userid)
                val response = authRepository.register(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Registration successful, handle the token
                        _registrationSuccess.postValue(Event("註冊成功"))
                    } else {
                        _registrationError.postValue(Event("註冊失敗"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            val message = json.getString("message")
                            _registrationError.postValue(Event("註冊失敗: $message"))
                        } catch (e: Exception) {
                            _registrationError.postValue(Event("註冊失敗: ${response.code()} ${response.message()}"))
                        }
                    } else {
                        _registrationError.postValue(Event("註冊失敗: ${response.code()} ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                _registrationError.postValue(Event("註冊失敗: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
