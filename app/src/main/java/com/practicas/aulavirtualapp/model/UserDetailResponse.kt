package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName


data class UserDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("fullname") val fullname: String,
    @SerializedName("email") val email: String,

    @SerializedName("city") val city: String? = "",
    @SerializedName("country") val country: String? = "",
    @SerializedName("department") val department: String? = "",
    @SerializedName("institution") val institution: String? = "",
    @SerializedName("description") val description: String? = "",
    @SerializedName("phone1") val phone: String? = "",
    @SerializedName("phone2") val mobile: String? = "",
    @SerializedName("address") val address: String? = "",
    @SerializedName("profileimageurl") val profileImageUrl: String,

    // Moodle a veces manda roles, a veces no. Lo dejamos opcional.
    @SerializedName("roles") val roles: List<Role>? = emptyList()
)

data class Role(
    @SerializedName("roleid") val roleId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortname") val shortName: String
)