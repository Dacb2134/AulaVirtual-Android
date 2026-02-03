package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class UserDetailResponse(
    @SerializedName("users") val users: List<UserDetail>
)

data class UserDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("fullname") val fullname: String,
    @SerializedName("email") val email: String,

    // Ubicación
    @SerializedName("city") val city: String? = "",
    @SerializedName("country") val country: String? = "",

    // Académico
    @SerializedName("department") val department: String? = "",
    @SerializedName("institution") val institution: String? = "",

    // Contacto y Descripción
    @SerializedName("description") val description: String? = "",
    @SerializedName("phone1") val phone: String? = "",
    @SerializedName("phone2") val mobile: String? = "",
    @SerializedName("address") val address: String? = "",

    // Imágenes
    @SerializedName("profileimageurl") val profileImageUrl: String,

    // 👇 NUEVO: Lista de Roles (Para saber si es Docente o Estudiante)
    @SerializedName("roles") val roles: List<Role>? = emptyList()
)

// Estructura del Rol
data class Role(
    @SerializedName("roleid") val roleId: Int,
    @SerializedName("name") val name: String,       // Ej: "Student"
    @SerializedName("shortname") val shortName: String // Ej: "student" o "editingteacher"
)