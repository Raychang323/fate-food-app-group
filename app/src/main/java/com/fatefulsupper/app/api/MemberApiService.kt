package com.fatefulsupper.app.api
import com.fatefulsupper.app.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query
interface MemberApiService {
    // 驗證 Email 驗證碼
    @POST("/api/verifyEmailCode")
    suspend fun verifyEmailCode(
        @Query("userid") userid: String,
        @Query("code") code: String
    ): Response<ApiResponse>

    // 重新寄送驗證碼
    @POST("/api/resendEmailCode")
    suspend fun resendEmailCode(
        @Query("userid") userid: String
    ): Response<ApiResponse>
}