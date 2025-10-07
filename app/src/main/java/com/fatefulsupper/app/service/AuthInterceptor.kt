package com.fatefulsupper.app.service

import android.content.Context
import com.fatefulsupper.app.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = SessionManager.getAuthToken(context.applicationContext)

        val requestBuilder = originalRequest.newBuilder()
        // If the request is not for auth endpoints and token is available, add the header
        if (!originalRequest.url.encodedPath.contains("api/v1/auth") && token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
