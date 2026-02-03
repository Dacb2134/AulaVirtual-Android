package com.practicas.aulavirtualapp.repository

import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call

class AuthRepository {

    private val apiService = RetrofitClient.instance

    // Login
    fun login(user: String, pass: String): Call<TokenResponse> {
        return apiService.login(user, pass)
    }

    // Pedir Info del Usuario (Rescate de ID)
    fun getSiteInfo(token: String): Call<SiteInfoResponse> {
        return apiService.getSiteInfo(token)
    }

    // Pedir los Cursos
    fun getUserCourses(token: String, userId: Int): Call<List<Course>> {
        return apiService.getUserCourses(token, userId)
    }

    // Pedir las Tareas
    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> {
        return apiService.getCourseAssignments(token, courseId)
    }
}