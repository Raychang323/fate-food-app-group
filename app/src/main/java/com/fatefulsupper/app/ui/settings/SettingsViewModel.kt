package com.fatefulsupper.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    fun updateNightSnackBlacklist(userId: String, blacklistedIds: Set<String>) {
        viewModelScope.launch {
            try {
                val integerIds = blacklistedIds.mapNotNull { it.toIntOrNull() }
                val request = UpdateBlacklistRequest(integerIds)
                RetrofitClient.apiService.updateBlacklist(userId, request)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}