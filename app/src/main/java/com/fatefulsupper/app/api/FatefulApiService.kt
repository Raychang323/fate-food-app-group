package com.fatefulsupper.app.api

import com.fatefulsupper.app.data.model.AddBlacklistRequest
import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import com.fatefulsupper.app.data.response.GetBlacklistResponse
import com.fatefulsupper.app.data.model.request.LoginRequest
import com.fatefulsupper.app.data.model.request.RegisterRequest
import com.fatefulsupper.app.data.model.response.AuthResponse
import com.fatefulsupper.app.data.model.ApiResponse
import com.fatefulsupper.app.data.model.request.LuckyFoodRequest
import com.fatefulsupper.app.data.model.response.LuckyFoodResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FatefulApiService {

    // 呼叫 POST /api/members/{userid}/blacks
    /**
     * 將一個美食類別 ID 加入指定會員的黑名單。
     * @param userId 會員的 ID
     * @param request 包含 categoryId 的請求主體
     * @return 成功後通常會返回一個成功訊息或更新後的列表
     */
    @POST("members/{userId}/blacks")
    suspend fun addBlacklistItem(
        @Path("userId") userId: String,
        @Body request: AddBlacklistRequest
    ): Response<GetBlacklistResponse> // 假設成功回應返回更新後的列表

    // 呼叫 GET /api/members/{userid}/blacks
    /**
     * 取得指定會員的黑名單列表。
     * @param userId 會員的 ID
     * @return 包含黑名單詳情的 GetBlacklistResponse
     */
    @GET("members/{userId}/blacks")
    suspend fun getBlacklist(
        @Path("userId") userId: String
    ): Response<GetBlacklistResponse>

    // 呼叫 PUT /api/members/{userid}/blacks
    /**
     * 更新指定會員的黑名單列表，用新的列表完全覆蓋舊的列表。
     * @param userId 會員的 ID
     * @param request 包含所有要更新的黑名單類別 ID 的請求主體
     * @return 成功後通常會返回一個成功訊息或更新後的列表
     */
    @PUT("members/{userId}/blacks")
    suspend fun updateBlacklist(
        @Path("userId") userId: String,
        @Body request: UpdateBlacklistRequest
    ): Response<GetBlacklistResponse> // 假設成功回應返回更新後的列表

    // 以下是重新引入的認證和電子郵件驗證 API 方法
    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("verifyEmailCode")
    suspend fun verifyEmailCode(
        @Query("userid") userid: String,
        @Query("code") code: String
    ): Response<ApiResponse>

    @POST("resendEmailCode")
    suspend fun resendEmailCode(
        @Query("userid") userid: String
    ): Response<ApiResponse>

    // 幸運食物
    @POST("luckyfood/test/location")
    suspend fun getMemberLuckyFood(@Body request: LuckyFoodRequest): Response<LuckyFoodResponse>

    @POST("luckyfood/location")
    suspend fun getGuestLuckyFood(@Body request: LuckyFoodRequest): Response<LuckyFoodResponse>
}