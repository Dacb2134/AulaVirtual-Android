package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class SiteInfoResponse(
    @SerializedName("userid") val userid: Int,
    @SerializedName("fullname") val fullname: String,
    @SerializedName("userpictureurl") val userPictureUrl: String? = null,
    @SerializedName("userissiteadmin") val isSiteAdmin: Boolean = false
)