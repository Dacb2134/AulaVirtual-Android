package com.practicas.aulavirtualapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (exception: IOException) {
            Log.e("NetworkError", "Fallo de red en ${chain.request().url}", exception)
            throw exception
        }
    }
}
