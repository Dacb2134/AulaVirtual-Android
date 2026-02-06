package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Forum(
    val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("intro") val intro: String, // Viene en HTML
    @SerializedName("numdiscussions") val numDiscussions: Int,
    @SerializedName("cmid") val courseModuleId: Int, // ID para la API de permisos
    @SerializedName("cancreatediscussions") val canCreateDiscussions: Boolean? = false
) : Serializable