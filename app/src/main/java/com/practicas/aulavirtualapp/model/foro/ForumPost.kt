package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ForumPostResponse(
    @SerializedName("posts") val posts: List<ForumPost>
)

data class ForumPost(
    val id: Int,
    val subject: String,
    val message: String,
    @SerializedName("timecreated") val timeCreated: Long,
    val author: ForumAuthor,
    @SerializedName("parentid") val parentId: Int? = null, // Si es respuesta a otro
    @SerializedName("hasparent") val hasParent: Boolean = false
) : Serializable

data class ForumAuthor(
    val id: Int,
    val fullname: String,
    @SerializedName("urls") val urls: AuthorUrls
) : Serializable

data class AuthorUrls(
    @SerializedName("profileimage") val profileImage: String
) : Serializable