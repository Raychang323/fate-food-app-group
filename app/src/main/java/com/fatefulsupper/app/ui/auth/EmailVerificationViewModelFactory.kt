package com.fatefulsupper.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmailVerificationViewModelFactory(private val userid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailVerificationViewModel(userid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
