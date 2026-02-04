package com.practicas.aulavirtualapp.network

import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.model.EnrolledUser
import com.practicas.aulavirtualapp.model.GradeReportResponse
import com.practicas.aulavirtualapp.model.MoodleUploadFile
import com.practicas.aulavirtualapp.model.OAuthTokenResponse
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface MoodleApiService {

    @GET("login/token.php")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("service") service: String = "moodle_mobile_app"
    ): Call<TokenResponse>

    @FormUrlEncoded
    @POST("login/oauth2/token.php")
    fun loginWithOAuth(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") authCode: String,
        @Field("redirect_uri") redirectUri: String
    ): Call<OAuthTokenResponse>

    @POST("webservice/rest/server.php?wsfunction=core_webservice_get_site_info&moodlewsrestformat=json")
    fun getSiteInfo(@Query("wstoken") token: String): Call<SiteInfoResponse>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getUserDetails(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "core_user_get_users_by_field",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("field") field: String = "id",
        @Field("values[0]") userId: Int
    ): Call<List<UserDetail>>

    @GET("webservice/rest/server.php")
    fun getUserCourses(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<Course>>

    @GET("webservice/rest/server.php")
    fun getCourseAssignments(
        @Query("wstoken") token: String,
        @Query("courseids[0]") courseId: Int,
        @Query("includeconfigs") includeConfigs: Int = 1,
        @Query("wsfunction") function: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<AssignmentResponse>

    @Multipart
    @POST("webservice/upload.php")
    fun uploadAssignmentFile(
        @Part("token") token: RequestBody,
        @Part("filepath") filepath: RequestBody,
        @Part("itemid") itemId: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<List<MoodleUploadFile>>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun saveAssignmentSubmission(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_assign_save_submission",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("assignmentid") assignmentId: Int,
        @Field("plugindata[onlinetext_editor][text]") text: String? = null,
        @Field("plugindata[onlinetext_editor][format]") textFormat: Int? = 1,
        @Field("plugindata[files_filemanager]") fileManagerId: Int? = null
    ): Call<SaveSubmissionResponse>

    @GET("webservice/rest/server.php")
    fun getCourseContents(
        @Query("wstoken") token: String,
        @Query("courseid") courseId: Int,
        @Query("wsfunction") function: String = "core_course_get_contents",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<CourseSection>>

    @GET("webservice/rest/server.php")
    fun getUserBadges(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_badges_get_user_badges",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<BadgeResponse>

    @GET("webservice/rest/server.php")
    fun getGradeReport(
        @Query("wstoken") token: String,
        @Query("courseid") courseId: Int,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "gradereport_user_get_grade_items",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<GradeReportResponse>

    @GET("webservice/rest/server.php")
    fun getEnrolledUsers(
        @Query("wstoken") token: String,
        @Query("courseid") courseId: Int,
        @Query("wsfunction") function: String = "core_enrol_get_enrolled_users",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<List<EnrolledUser>>


    @GET("webservice/rest/server.php")
    fun getFilesInfo(
        @Query("wstoken") token: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") function: String = "core_user_get_private_files_info",
        @Query("moodlewsrestformat") format: String = "json"
    ): Call<PrivateFilesInfo>
}
