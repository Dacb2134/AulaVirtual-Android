package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("id") val id: Int,
    @SerializedName("fullname") val fullName: String,
    @SerializedName("shortname") val shortName: String,

    var color: String = "#6200EE"
)