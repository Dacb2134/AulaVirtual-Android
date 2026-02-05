package com.practicas.aulavirtualapp.repository

import android.util.Log
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.model.EnrolledUser
import com.practicas.aulavirtualapp.model.GradeReportResponse
import com.practicas.aulavirtualapp.model.MoodleFile
import com.practicas.aulavirtualapp.model.MoodleUploadFile
import com.practicas.aulavirtualapp.model.OAuthTokenResponse
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail
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

    // En tu AuthRepository.kt ...

    fun getAssignments(token: String, courseId: Int): Call<AssignmentResponse> {

        val params = mapOf(
            "courseids[0]" to courseId.toString()
        )

        // Usamos el método getCourseAssignments que ya configuramos con @QueryMap
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
        return apiService.saveAssignmentSubmission(
            token = token,
            assignmentId = assignmentId,
            text = text,
            fileManagerId = fileManagerId
        )
    }
}
