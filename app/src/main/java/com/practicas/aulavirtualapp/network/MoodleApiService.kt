package com.practicas.aulavirtualapp.network

import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MoodleApiService {

    // Login (Obtener Token)
    @GET("login/token.php")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("service") service: String = "moodle_mobile_app"
    ): Call<TokenResponse>

    //  Obtener información de mi usuario (ID, Nombre, Foto)

    @GET("webservice/rest/server.php")
    fun getSiteInfo(
        @Query("wstoken") token: String,
        @Query("wsfunction") function: String = "core_webservice_get_site_info",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<SiteInfoResponse>

    // Obtener MIS cursos
    @GET("webservice/rest/server.php")
    fun getUserCourses(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<Course>>

    // Obtener TAREAS de un curso específico
    @GET("webservice/rest/server.php")
    fun getCourseAssignments(
        @Query("wstoken") token: String,
        @Query("courseids[0]") courseId: Int,
        @Query("wsfunction") function: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<AssignmentResponse>
}