package com.fatefulsupper.app.ui.luckyfood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.request.LuckyFoodRequest
import com.fatefulsupper.app.data.model.response.LuckyFoodResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LuckyFoodViewModel(application: Application) : AndroidViewModel(application) {

    private val _luckyFoodResult = MutableLiveData<LuckyFoodResponse?>()
    val luckyFoodResult: LiveData<LuckyFoodResponse?> = _luckyFoodResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val api = RetrofitClient.getInstance(application)

    fun fetchLuckyFood(latitude: Double, longitude: Double, isMember: Boolean) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val request = LuckyFoodRequest(latitude, longitude)
            try {
                val response = if (isMember) {
                    api.getMemberLuckyFood(request)
                } else {
                    api.getGuestLuckyFood(request)
                }

                if (response.isSuccessful) {
                    _luckyFoodResult.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()} ${response.message()}"
                    _luckyFoodResult.value = null
                }
            } catch (e: IOException) {
                _error.value = "網路錯誤，請稍後再試。"
                _luckyFoodResult.value = null
            } catch (e: HttpException) {
                _error.value = "伺服器錯誤，請稍後再試。"
                _luckyFoodResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
