package com.practicas.aulavirtualapp.repository

import android.util.Log
import com.practicas.aulavirtualapp.model.*
import com.practicas.aulavirtualapp.network.PrivateFilesInfo
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class AuthRepository {

    // Instancia del cliente Retrofit
    private val apiService = RetrofitClient.instance

    // --- Login y Sitio ---
    fun login(user: String, pass: String): Call<TokenResponse> = apiService.login(user, pass)

    fun loginWithOAuth(authCode: String, redirectUri: String): Call<OAuthTokenResponse> =
        apiService.loginWithOAuth(authCode = authCode, redirectUri = redirectUri)

    fun getSiteInfo(token: String): Call<SiteInfoResponse> = apiService.getSiteInfo(token)

    // --- Cursos y Tareas (Agenda) ---

    fun getUserCourses(token: String, userId: Int): Call<List<Course>> = apiService.getUserCourses(token, userId)

    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> {
        val params = mapOf(
            "courseids[0]" to courseId.toString()
        )
        return apiService.getCourseAssignments(token, params)
    }

    fun getCourseContents(token: String, courseId: Int): Call<List<CourseSection>> =
        apiService.getCourseContents(token, courseId)

    // --- Perfil de Usuario  ---

    fun getUserDetails(token: String, userId: Int): Call<List<UserDetail>> {
        Log.d("AuthRepository", "Pidiendo perfil (Lista) para ID: $userId")
        return apiService.getUserDetails(token = token, userId = userId)
    }

    // --- Extras ---
    fun getUserBadges(token: String, userId: Int): Call<BadgeResponse> = apiService.getUserBadges(token, userId)

    fun getGradeReport(token: String, courseId: Int, userId: Int): Call<GradeReportResponse> =
        apiService.getGradeReport(token, courseId, userId)

    fun getEnrolledUsers(token: String, courseId: Int): Call<List<EnrolledUser>> =
        apiService.getEnrolledUsers(token, courseId)

    fun getFilesInfo(token: String, userId: Int): Call<PrivateFilesInfo> {
        return apiService.getFilesInfo(token, userId)
    }
    fun getSubmissionStatus(token: String, assignmentId: Int): Call<SubmissionStatusResponse> {
        return apiService.getSubmissionStatus(token, assignmentId)
    }

    fun uploadAssignmentFile(
        token: RequestBody,
        filepath: RequestBody,
        itemId: RequestBody,
        file: MultipartBody.Part
    ): Call<List<MoodleUploadFile>> {
        return apiService.uploadAssignmentFile(token, filepath, itemId, file)
    }

    fun saveAssignmentSubmission(
        token: String,
        assignmentId: Int,
        text: String?,
        fileManagerId: Int?
    ): Call<SaveSubmissionResponse> {

        val finalText = if (text.isNullOrBlank()) null else text
        val finalFormat = if (finalText == null) null else 1 // HTML
        val finalItemId = if (finalText == null) null else 0 // 👈 OBLIGATORIO PARA MOODLE

        return apiService.saveAssignmentSubmission(
            token = token,
            assignmentId = assignmentId,
            text = finalText,
            textFormat = finalFormat,
            textItemId = finalItemId,
            fileManagerId = fileManagerId
        )
    }

    // FUNCIONES PARA GOOGLE  ---


    // 1. Verificar si el email ya existe en Moodle
    fun checkUserExists(masterToken: String, email: String): Call<List<UserDetail>> {
        return apiService.getUserByEmail(masterToken, "core_user_get_users_by_field", "json", "email", email)
    }

    // 2. Registrar usuario nuevo en Moodle
    fun registerUser(masterToken: String, email: String, fullName: String): Call<List<UserDetail>> {
        val parts = fullName.trim().split(" ")
        val firstName = parts.firstOrNull() ?: "Usuario"
        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Google"

        // Contraseña técnica (usuario entra con Google, no necesita saberla)
        val dummyPass = "GoogleUser_2026!${System.currentTimeMillis()}"

        return apiService.createUser(
            token = masterToken,
            username = email.lowercase(),
            password = dummyPass,
            firstName = firstName,
            lastName = lastName,
            email = email
        )
    }
}