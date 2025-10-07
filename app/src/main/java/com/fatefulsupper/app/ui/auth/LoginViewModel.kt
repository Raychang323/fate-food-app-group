package com.fatefulsupper.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.util.Event
import com.fatefulsupper.app.util.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Event<Pair<String, String>>>()
    val loginSuccess: LiveData<Event<Pair<String, String>>> = _loginSuccess

    private val _loginError = MutableLiveData<Event<String>>()
    val loginError: LiveData<Event<String>> = _loginError

    fun login(userid: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.getInstance(getApplication()).login(userid, password).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val token = responseBody?.get("token") as? String
                    if (token != null) {
                        _loginSuccess.postValue(Event(Pair(userid, token)))
                    } else {
                        _loginError.postValue(Event("登入失敗: 無法取得 token"))
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
