package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ForumDiscussionResponse(
    @SerializedName("discussions") val discussions: List<ForumDiscussion>
)

data class ForumDiscussion(
    val id: Int,
    val name: String,
    val message: String,
    @SerializedName("userfullname") val userFullName: String,
    @SerializedName("userpictureurl") val userPictureUrl: String,
    @SerializedName("created") val created: Long, // Fecha Unix
    @SerializedName("numreplies") val numReplies: Int,
    val pinned: Boolean = false,
    @SerializedName("canreply") val canReply: Boolean = true
) : Serializable