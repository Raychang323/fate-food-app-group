package com.fatefulsupper.app.data.repository

import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.service.AuthService
import com.fatefulsupper.app.service.RetrofitClient

class AuthRepository {

    private val authService: AuthService = RetrofitClient.instance.create(AuthService::class.java)

    suspend fun register(request: RegisterRequest) = authService.register(request)

    suspend fun login(request: LoginRequest) = authService.login(request)
}
