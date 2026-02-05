package com.practicas.aulavirtualapp.model

data class LoginResult(
    val token: String,
    val userId: Int,
    val role: UserRole
)
