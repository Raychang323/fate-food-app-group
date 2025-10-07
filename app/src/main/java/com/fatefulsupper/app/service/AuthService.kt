package com.fatefulsupper.app.service

import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.data.model.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
