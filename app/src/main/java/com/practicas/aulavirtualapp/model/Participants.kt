package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class EnrolledUser(
    @SerializedName("id") val id: Int,
    @SerializedName("fullname") val fullName: String?,
    @SerializedName("profileimageurl") val profileImageUrl: String?,
    @SerializedName("roles") val roles: List<ParticipantRole> = emptyList()
)

data class ParticipantRole(
    @SerializedName("shortname") val shortName: String?,
    @SerializedName("name") val name: String?
)
