package com.practicas.aulavirtualapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // 👈 IMPORTANTE: Asegúrate de que esto se importe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val baseUrl: String = "http://192.168.1.144/"

    val instance: MoodleApiService by lazy {

        // Configuramos el Espía (Interceptor)
        val logging = HttpLoggingInterceptor().apply {
            // "BODY" es el nivel máximo: muestra URL, headers y el JSON completo
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging) // 👈 ¡AQUÍ LO CONECTAMOS! (Ponlo primero para ver todo)
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