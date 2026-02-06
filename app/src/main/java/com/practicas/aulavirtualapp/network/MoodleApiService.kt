package com.practicas.aulavirtualapp.network

import com.practicas.aulavirtualapp.model.AddDiscussionResponse
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.model.EnrolledUser
import com.practicas.aulavirtualapp.model.Forum
import com.practicas.aulavirtualapp.model.ForumDiscussionResponse
import com.practicas.aulavirtualapp.model.ForumPostResponse
import com.practicas.aulavirtualapp.model.GradeReportResponse
import com.practicas.aulavirtualapp.model.MoodleUploadFile
import com.practicas.aulavirtualapp.model.OAuthTokenResponse
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.SubmissionStatusResponse
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
import retrofit2.http.QueryMap

interface MoodleApiService {

    // --- NUEVO: AUTO-REGISTRO CON GOOGLE ---

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getUserByEmail(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "core_user_get_users_by_field",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("field") field: String = "email",
        @Field("values[0]") email: String
    ): Call<List<UserDetail>>

    // Crear usuario nuevo en Moodle
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun createUser(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "core_user_create_users",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("users[0][username]") username: String,
        @Field("users[0][password]") password: String,
        @Field("users[0][firstname]") firstName: String,
        @Field("users[0][lastname]") lastName: String,
        @Field("users[0][email]") email: String,
        @Field("users[0][auth]") auth: String = "manual"
    ): Call<List<UserDetail>>

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
        @QueryMap(encoded = true) params: Map<String, String>,
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

        // TEXTO EN LÍNEA (OBLIGATORIO itemid)
        @Field("plugindata[onlinetext_editor][text]") text: String? = null,
        @Field("plugindata[onlinetext_editor][format]") textFormat: Int? = null,
        @Field("plugindata[onlinetext_editor][itemid]") textItemId: Int? = null,

        //ARCHIVOS
        @Field("plugindata[files_filemanager]") fileManagerId: Int? = null
    ): Call<SaveSubmissionResponse>


    @GET("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_get_submission_status")
    fun getSubmissionStatus(
        @Query("wstoken") token: String,
        @Query("assignid") assignmentId: Int
    ): Call<SubmissionStatusResponse>


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

    // --- ZONA DE FOROS ---

    // 1. Obtener foros del curso
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getForumsByCourse(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_get_forums_by_courses",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("courseids[0]") courseId: Int
    ): Call<List<Forum>>

    //  Obtener discusiones de un foro
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getForumDiscussions(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_get_forum_discussions",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("forumid") forumId: Int,
        @Field("sortorder") sortOrder: Int = 1 // 1 = Más recientes primero
    ): Call<ForumDiscussionResponse>

    //  Obtener los posts de una discusión (el chat completo)
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getDiscussionPosts(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_get_discussion_posts",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("discussionid") discussionId: Int
    ): Call<ForumPostResponse>

    // Crear nueva discusión
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun addDiscussion(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_add_discussion",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("forumid") forumId: Int,
        @Field("subject") subject: String,
        @Field("message") message: String,
        @Field("groupid") groupId: Int = -1
    ): Call<AddDiscussionResponse>

    //  Responder a un post
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun addDiscussionPost(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_add_discussion_post",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("postid") postId: Int,
        @Field("subject") subject: String,
        @Field("message") message: String
    ): Call<AddDiscussionResponse>

    // Verificar permisps- futura mejora para docente
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    fun getForumAccess(
        @Field("wstoken") token: String,
        @Field("wsfunction") function: String = "mod_forum_get_forum_access_information",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("forumid") forumId: Int
    ): Call<Map<String, Any>> // Devuelve mapa de permisos (canstartdiscussion, etc)




}