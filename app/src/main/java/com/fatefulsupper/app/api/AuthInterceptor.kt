package com.fatefulsupper.app.api

import android.content.Context
import com.fatefulsupper.app.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val tokenManager = TokenManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val requestBuilder = originalRequest.newBuilder()
        if (token != null && !originalRequest.url.encodedPath.contains("/login") && !originalRequest.url.encodedPath.contains("/register")) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
