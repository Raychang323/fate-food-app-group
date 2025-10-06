package com.fatefulsupper.app.api

import com.fatefulsupper.app.data.model.UpdateBlacklistRequest
import retrofit2.Response
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Body

interface ApiService {

    @PUT("members/{userId}/blacks/batch") // 假設後端會新增一個 '/batch' 端點來處理批次更新
    suspend fun updateBlacklist(
        @Path("userId") userId: String,
        @Body request: UpdateBlacklistRequest
    ): Response<Void> // 假設後端成功後只返回 200 OK，沒有特定的響應體

    // 你也可以在這裡加入獲取黑名單的 GET 方法，例如：
    // @GET("members/{userId}/blacks")
    // suspend fun getBlacklist(@Path("userId") userId: String): Response<List<FoodCategory>>
    // 但你需要先定義 FoodCategory 這個資料模型
}
