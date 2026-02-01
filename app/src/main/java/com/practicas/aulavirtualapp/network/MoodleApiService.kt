package com.practicas.aulavirtualapp.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MoodleApiService {

    // 👇 ESTA ES LA NUEVA FUNCIÓN DE LOGIN
    @GET("login/token.php")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("service") service: String = "moodle_mobile_app"
    ): Call<TokenResponse>


    @GET("webservice/rest/server.php")
    fun getSiteInfo(
        @Query("wstoken") token: String,
        @Query("wsfunction") function: String = "core_webservice_get_site_info",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<JsonObject>
}