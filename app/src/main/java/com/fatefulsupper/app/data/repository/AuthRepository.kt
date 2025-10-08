package com.fatefulsupper.app.data.repository

import android.content.Context
import com.fatefulsupper.app.api.FatefulApiService
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest

class AuthRepository(context: Context) {

    private val authService: FatefulApiService = RetrofitClient.getInstance(context)

    suspend fun register(request: RegisterRequest) = authService.register(request)

    suspend fun login(request: LoginRequest) = authService.login(request)
}