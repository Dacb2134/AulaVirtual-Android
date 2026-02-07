package com.practicas.aulavirtualapp.model.course

import com.google.gson.annotations.SerializedName

data class CourseSection(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("section") val section: Int?,
    @SerializedName("visible") val visible: Int?,
    @SerializedName("modules") val modules: List<CourseModule> = emptyList()
)

data class CourseModule(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("modname") val modName: String?,
    @SerializedName("modplural") val modPlural: String?,
    @SerializedName("visible") val visible: Int?
)
