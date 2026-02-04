package com.practicas.aulavirtualapp.network

import okhttp3.Interceptor
import okhttp3.Response

class MoodleFormatInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val isRestServer = url.encodedPath.endsWith("webservice/rest/server.php")
        val hasFormat = url.queryParameter("moodlewsrestformat") != null
        if (!isRestServer || hasFormat) {
            return chain.proceed(request)
        }
        val newUrl = url.newBuilder()
            .addQueryParameter("moodlewsrestformat", "json")
            .build()
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}
