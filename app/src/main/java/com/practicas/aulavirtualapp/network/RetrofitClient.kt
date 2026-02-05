package com.practicas.aulavirtualapp.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


class MoodleSubmissionDeserializer : JsonDeserializer<SaveSubmissionResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SaveSubmissionResponse {
        // CASO 1: Moodle devuelve [] (Array vacío). Significa "ÉXITO TOTAL".
        if (json.isJsonArray) {
            return SaveSubmissionResponse(status = true, warnings = emptyList())
        }

        // CASO 2: Moodle devuelve {} (Objeto). Lo leemos normal.
        return try {
            // Usamos un Gson interno nuevo para evitar bucles infinitos
            val gson = GsonBuilder().create()
            gson.fromJson(json, SaveSubmissionResponse::class.java)
        } catch (e: Exception) {
            // Si falla, asumimos éxito para no bloquear al usuario
            SaveSubmissionResponse(status = true, warnings = emptyList())
        }
    }
}

object RetrofitClient {

    const val baseUrl: String = "http://192.168.1.144/"

    val instance: MoodleApiService by lazy {

        // Configuramos el Espía (Interceptor)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(NetworkErrorInterceptor())
            .addInterceptor(TokenExpiryInterceptor())
            .addInterceptor(MoodleFormatInterceptor())
            .build()

        // 🛠️ CONFIGURAMOS EL GSON CON EL TRUCO PARA MOODLE
        val gson = GsonBuilder()
            // Le decimos: "Si ves SaveSubmissionResponse, usa mi desearializador mágico"
            .registerTypeAdapter(SaveSubmissionResponse::class.java, MoodleSubmissionDeserializer())
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            // 👇 IMPORTANTE: Pasamos el 'gson' personalizado aquí
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(MoodleApiService::class.java)
    }
}