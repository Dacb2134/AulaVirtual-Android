package com.practicas.aulavirtualapp.model.auth

import com.practicas.aulavirtualapp.model.auth.UserRole

data class LoginResult(
    val token: String,
    val userId: Int,
    val role: UserRole
)