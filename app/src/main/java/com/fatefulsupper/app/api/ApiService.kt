package com.fatefulsupper.app.api

import com.fatefulsupper.app.data.model.ApiResponse
import com.fatefulsupper.app.data.model.BlacklistResponse
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.data.model.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FatefulApiService {

    @PUT("members/{userId}/blacks/batch")
    suspend fun updateBlacklist(
        @Path("userId") userId: String,
        @Body request: UpdateBlacklistRequest
    ): Response<Void>

    @GET("members/{userId}/blacks")
    suspend fun getBlacklist(
        @Path("userId") userId: String
    ): Response<BlacklistResponse>

    @POST("verifyEmailCode")
    suspend fun verifyEmailCode(
        @Query("userid") userid: String,
        @Query("code") code: String
    ): Response<ApiResponse>

    @POST("resendEmailCode")
    suspend fun resendEmailCode(
        @Query("userid") userid: String
    ): Response<ApiResponse>

    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}