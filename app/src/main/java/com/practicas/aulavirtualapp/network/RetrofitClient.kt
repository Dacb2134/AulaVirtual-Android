package com.practicas.aulavirtualapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val baseUrl: String = "http://192.168.1.144/"

    val instance: MoodleApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(NetworkErrorInterceptor())
            .addInterceptor(TokenExpiryInterceptor())
            .addInterceptor(MoodleFormatInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MoodleApiService::class.java)
    }
}
