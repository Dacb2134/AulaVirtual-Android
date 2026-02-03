package com.practicas.aulavirtualapp.network

import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.MoodleFile
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail // Importamos directo el modelo del usuario
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MoodleApiService {

    // ... (Login y SiteInfo quedan igual) ...
    @GET("login/token.php")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("service") service: String = "moodle_mobile_app"
    ): Call<TokenResponse>

    @POST("webservice/rest/server.php?wsfunction=core_webservice_get_site_info&moodlewsrestformat=json")
    fun getSiteInfo(@Query("wstoken") token: String): Call<SiteInfoResponse>

    // 👇 CAMBIO IMPORTANTE: Devuelve una LISTA directa, no un objeto Response
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getUserDetails(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "core_user_get_users_by_field",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("field") field: String = "id",
        @Field("values[0]") userId: Int
    ): Call<List<UserDetail>>

    // ... (El resto de funciones UserCourses, Assignments, etc. quedan igual) ...
    @GET("webservice/rest/server.php")
    fun getUserCourses(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<Course>>

    // ... (Asegúrate de tener el resto de tus funciones aquí) ...
    @GET("webservice/rest/server.php")
    fun getCourseAssignments(
        @Query("wstoken") token: String,
        @Query("courseids[0]") courseId: Int,
        @Query("wsfunction") function: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<AssignmentResponse>

    @GET("webservice/rest/server.php")
    fun getUserBadges(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_badges_get_user_badges",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<BadgeResponse>

    @GET("webservice/rest/server.php")
    fun getUserFiles(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_user_get_private_files",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<MoodleFile>>
}