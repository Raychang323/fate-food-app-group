package com.fatefulsupper.app.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    /**
     * 將更新後的宵夜黑名單發送到後端。
     * @param userId 當前使用者的 ID。
     * @param selectedCategoryIds 被選中的宵夜類別 ID 清單。
     */
    fun updateNightSnackBlacklist(userId: String, selectedCategoryIds: List<Int>) {
        viewModelScope.launch {
            try {
                val request = UpdateBlacklistRequest(categoryIds = selectedCategoryIds)
                val response = RetrofitClient.apiService.updateBlacklist(userId, request)

                if (response.isSuccessful) {
                    Log.d(TAG, "Blacklist updated successfully for user $userId. Response code: ${response.code()}")
                    // TODO: 可以通過 LiveData 或 StateFlow 更新 UI 狀態，例如顯示成功訊息。
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error updating blacklist for user $userId: ${response.code()} - $errorBody")
                    // TODO: 可以通過 LiveData 或 StateFlow 更新 UI 狀態，例如顯示錯誤訊息。
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during blacklist update for user $userId: ${e.message}", e)
                // TODO: 可以通過 LiveData 或 StateFlow 更新 UI 狀態，例如顯示網路錯誤訊息。
            }
        }
    }

    // TODO: 如果需要，可以在這裡添加 LiveData/StateFlow 來暴露更新狀態給 UI。
}
