package com.practicas.aulavirtualapp.repository

import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.SiteInfo
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call

class AuthRepository {

    // Login
    fun login(user: String, pass: String): Call<TokenResponse> {
        return RetrofitClient.instance.login(user, pass)
    }

    // Pedir Info del Usuario (para saber su ID)
    fun getSiteInfo(token: String): Call<SiteInfo> {
        return RetrofitClient.instance.getSiteInfo(token)
    }

    // Pedir los Cursos
    fun getUserCourses(token: String, userId: Int): Call<List<Course>> {
        return RetrofitClient.instance.getUserCourses(token, userId)
    }

    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> {
        return RetrofitClient.instance.getCourseAssignments(token, courseId)
    }
}