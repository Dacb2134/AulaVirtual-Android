package com.practicas.aulavirtualapp.model.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String,

    var userid: Int = 0
)