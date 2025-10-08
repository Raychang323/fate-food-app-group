package com.fatefulsupper.app.data.repository

import android.content.Context
import com.fatefulsupper.app.api.RetrofitClient
import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.service.AuthService

class AuthRepository(context: Context) {

    private val authService: AuthService = RetrofitClient.getInstance(context).create(AuthService::class.java)

    suspend fun register(request: RegisterRequest) = authService.register(request)

    suspend fun login(request: LoginRequest) = authService.login(request)
}
