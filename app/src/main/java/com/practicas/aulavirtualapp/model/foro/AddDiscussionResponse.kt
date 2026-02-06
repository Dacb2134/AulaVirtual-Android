package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class AddDiscussionResponse(
    @SerializedName("postid") val postId: Int,
    @SerializedName("warnings") val warnings: List<Any>?,
    @SerializedName("messages") val messages: List<MessageStatus>?
)

data class MessageStatus(
    @SerializedName("type") val type: String,
    @SerializedName("message") val message: String
)