package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("id") val id: Int,
    @SerializedName("fullname") val fullName: String,
    @SerializedName("shortname") val shortName: String,
    @SerializedName("idnumber") val idNumber: String? = null
)