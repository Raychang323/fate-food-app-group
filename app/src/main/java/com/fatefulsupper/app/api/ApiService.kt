package com.fatefulsupper.app.api

import com.fatefulsupper.app.data.model.ApiResponse
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FatefulApiService {

    @POST("login")
    fun login(@Query("userid") userid: String, @Query("password") password: String): Call<Map<String, Any>>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("userid") userid: String,
        @Field("password") password: String,
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("role") role: String
    ): Call<Map<String, Any>>

    @PUT("members/{userId}/blacks/batch")
    suspend fun updateBlacklist(
        @Path("userId") userId: String,
        @Body request: UpdateBlacklistRequest
    ): Response<Void>

    @POST("/api/verifyEmailCode")
    suspend fun verifyEmailCode(
        @Query("userid") userid: String,
        @Query("code") code: String
    ): Response<ApiResponse>

    @POST("/api/resendEmailCode")
    suspend fun resendEmailCode(
        @Query("userid") userid: String
    ): Response<ApiResponse>
}