package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class SiteInfo(
    @SerializedName("userid") val userId: Int,
    @SerializedName("fullname") val fullName: String,
    @SerializedName("sitename") val siteName: String,
    @SerializedName("userpictureurl") val userPictureUrl: String
)