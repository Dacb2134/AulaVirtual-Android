package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Forum(
    val id: Int,
    @SerializedName("course") val courseId: Int,
    @SerializedName("type") val type: String, // "news", "general", "qanda", etc.
    @SerializedName("name") val name: String,
    @SerializedName("intro") val intro: String, // Viene en HTML
    @SerializedName("numdiscussions") val numDiscussions: Int,
    @SerializedName("cmid") val courseModuleId: Int, // ID para la API de permisos
    @SerializedName("cancreatediscussions") val canCreateDiscussions: Boolean? = false,
    @SerializedName("duedate") val dueDate: Long = 0,
    @SerializedName("cutoffdate") val cutoffDate: Long = 0
) : Serializable