package com.practicas.aulavirtualapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class TokenExpiryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val contentType = body.contentType()
        if (contentType?.subtype != "json") {
            return response
        }
        val bodyString = body.string()
        if (bodyString.contains("invalidtoken", ignoreCase = true) ||
            bodyString.contains("expired", ignoreCase = true)
        ) {
            Log.w("TokenExpiry", "Token inválido o expirado detectado.")
            throw IOException("Token expirado o inválido.")
        }
        return response.newBuilder()
            .body(bodyString.toResponseBody(contentType))
            .build()
    }
}
