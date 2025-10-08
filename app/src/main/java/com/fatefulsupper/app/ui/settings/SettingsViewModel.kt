package com.fatefulsupper.app.ui.settings

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import com.fatefulsupper.app.util.SetupConstants
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    fun updateNightSnackBlacklist(userId: String, blacklistedIds: Set<String>) {
        viewModelScope.launch {
            try {
                val integerIds = blacklistedIds.mapNotNull { it.toIntOrNull() }
                val request = UpdateBlacklistRequest(integerIds)
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
                    val blacklistResponse = response.body()
                    if (blacklistResponse?.status == "success") {
                        val remoteBlacklistKeys = blacklistResponse.data.black_categories
                            .map { it.categoryKey }
                            .toSet()

                        with(prefs.edit()) {
                            putStringSet(SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, remoteBlacklistKeys)
                            apply()
                        }
                        Log.d("BlacklistSync", "Blacklist successfully synced from remote.")
                    } else {
                        Log.e("BlacklistSync", "API returned non-success status: ${blacklistResponse?.status}")
                    }
                } else {
                    Log.e("BlacklistSync", "Failed to sync blacklist: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("BlacklistSync", "Error syncing blacklist", e)
            }
        }
    }
}