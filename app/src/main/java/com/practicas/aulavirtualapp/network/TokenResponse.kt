package com.practicas.aulavirtualapp.network

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("privatetoken") val privateToken: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errorcode") val errorCode: String? = null
)