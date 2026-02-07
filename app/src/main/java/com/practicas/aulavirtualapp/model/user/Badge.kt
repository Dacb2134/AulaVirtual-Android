package com.practicas.aulavirtualapp.model.user

import com.google.gson.annotations.SerializedName

data class BadgeResponse(
    @SerializedName("badges") val badges: List<Badge>
)

data class Badge(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("badgeurl") val badgeUrl: String,
    @SerializedName("dateissued") val dateIssued: Long
)