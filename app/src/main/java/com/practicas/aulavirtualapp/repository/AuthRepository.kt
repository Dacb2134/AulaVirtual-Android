package com.practicas.aulavirtualapp.repository

import android.util.Log
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.MoodleFile
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail
import com.practicas.aulavirtualapp.network.PrivateFilesInfo
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call

class AuthRepository {

    // Instancia del cliente Retrofit
    private val apiService = RetrofitClient.instance

    // --- Login y Sitio ---
    fun login(user: String, pass: String): Call<TokenResponse> = apiService.login(user, pass)

    fun getSiteInfo(token: String): Call<SiteInfoResponse> = apiService.getSiteInfo(token)

    // --- Cursos y Tareas (Agenda) ---
    // Mantenemos getUserCourses igual
    fun getUserCourses(token: String, userId: Int): Call<List<Course>> = apiService.getUserCourses(token, userId)

    // ⚠️ IMPORTANTE: Mantenemos el nombre 'getAssignments' para no romper tu AgendaViewModel
    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> = apiService.getCourseAssignments(token, courseId)

    // --- Perfil de Usuario (LA ACTUALIZACIÓN) ---
    // 👇 AHORA devuelve Call<List<UserDetail>> para coincidir con el JSON [...] de Moodle
    fun getUserDetails(token: String, userId: Int): Call<List<UserDetail>> {
        Log.d("AuthRepository", "Pidiendo perfil (Lista) para ID: $userId")
        return apiService.getUserDetails(token = token, userId = userId)
    }

    // --- Extras ---
    fun getUserBadges(token: String, userId: Int): Call<BadgeResponse> = apiService.getUserBadges(token, userId)

    fun getFilesInfo(token: String, userId: Int): Call<PrivateFilesInfo> {
        return apiService.getFilesInfo(token, userId)
}}