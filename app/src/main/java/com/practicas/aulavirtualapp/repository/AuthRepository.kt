package com.practicas.aulavirtualapp.repository

import android.util.Log // Importante para ver errores
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.MoodleFile
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call

class AuthRepository {

    private val apiService = RetrofitClient.instance

    fun login(user: String, pass: String): Call<TokenResponse> = apiService.login(user, pass)
    fun getSiteInfo(token: String): Call<SiteInfoResponse> = apiService.getSiteInfo(token)
    fun getUserCourses(token: String, userId: Int): Call<List<Course>> = apiService.getUserCourses(token, userId)
    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> = apiService.getCourseAssignments(token, courseId)


    fun getUserDetails(token: String, userId: Int): Call<List<UserDetail>> {
        Log.d("AuthRepository", "Pidiendo perfil para ID: $userId")
        return apiService.getUserDetails(token = token, userId = userId)
    }

    fun getUserBadges(token: String, userId: Int): Call<BadgeResponse> = apiService.getUserBadges(token, userId)
    fun getUserFiles(token: String, userId: Int): Call<List<MoodleFile>> = apiService.getUserFiles(token, userId)
}