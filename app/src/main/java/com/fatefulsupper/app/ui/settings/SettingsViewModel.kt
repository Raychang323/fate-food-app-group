package com.fatefulsupper.app.ui.settings

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import com.fatefulsupper.app.data.response.GetBlacklistResponse
import com.fatefulsupper.app.util.SetupConstants
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    fun updateNightSnackBlacklist(userId: String, blacklistedIds: Set<Int>) { // Changed type to Set<Int>
        viewModelScope.launch {
            try {
                val request = UpdateBlacklistRequest(blacklistedIds.toList()) // Directly use the Int set
                RetrofitClient.getInstance(getApplication()).updateBlacklist(userId, request)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to update blacklist", e)
            }
        }
    }

    fun syncBlacklistOnLogin(userId: String, prefs: SharedPreferences) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getInstance(getApplication()).getBlacklist(userId)

                if (response.isSuccessful) {
                    val blacklistResponse: GetBlacklistResponse? = response.body()
                    if (blacklistResponse != null) {
                        // 檢查 status 欄位，並處理 data.blackCategories 可能為 null 的情況
                        if (blacklistResponse.status == "success") {
                            val remoteBlacklistKeys = blacklistResponse.data.blackCategories
                                ?.map { it.categoryKey }
                                ?.toSet() ?: emptySet() // 如果 blackCategories 為 null，則使用空集合

                            with(prefs.edit()) {
                                putStringSet(SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, remoteBlacklistKeys)
                                apply()
                            }
                            Log.d("BlacklistSync", "Blacklist successfully synced from remote.")
                        } else {
                            Log.e("BlacklistSync", "API returned non-success status: ${blacklistResponse.status}")
                        }
                    } else {
                        Log.e("BlacklistSync", "API returned null body.")
                    }
                } else {
                    Log.e("BlacklistSync", "Failed to sync blacklist: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("BlacklistSync", "Error syncing blacklist", e)
            } catch (e: Exception) {
                Log.e("BlacklistSync", "Error syncing blacklist", e)
            }
        }
    }
}