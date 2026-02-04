package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class EnrolledUser(
    @SerializedName("id") val id: Int,
    @SerializedName("fullname") val fullName: String?,
    @SerializedName("profileimageurl") val profileImageUrl: String?,
    @SerializedName("roles") val roles: List<UserRole> = emptyList()
)

data class UserRole(
    @SerializedName("shortname") val shortName: String?,
    @SerializedName("name") val name: String?
)
