package com.fatefulsupper.app.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/" // 注意：這是一個模擬器專用的 IP 地址
    // 如果你是在實體手機上測試，你需要將其更改為你後端的實際 IP 地址
    // 例如：private const val BASE_URL = "http://YOUR_SERVER_IP:8080/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY) // 設置日誌級別為 BODY，可以看到完整的請求和響應體
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 添加日誌攔截器
        .connectTimeout(30, TimeUnit.SECONDS) // 連接超時時間
        .readTimeout(30, TimeUnit.SECONDS)    // 讀取超時時間
        .writeTimeout(30, TimeUnit.SECONDS)   // 寫入超時時間
        .build()

    private val gson = GsonBuilder()
        .setLenient() // 設置為寬鬆模式以處理某些不規範的 JSON
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 連線超時
            .readTimeout(60, TimeUnit.SECONDS)    // 讀取超時
            .writeTimeout(60, TimeUnit.SECONDS)   // 寫入超時
            .build()
    }

    val apiService: MemberApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)                 // 使用自訂 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemberApiService::class.java)
    }
}
