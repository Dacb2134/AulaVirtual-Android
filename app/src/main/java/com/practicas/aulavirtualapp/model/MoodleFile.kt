package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class MoodleFile(
    @SerializedName("filename") val fileName: String,
    @SerializedName("filepath") val filePath: String? = "",
    @SerializedName("filesize") val fileSize: Int? = 0,


    @SerializedName("fileurl") val fileUrl: String? = "",

    @SerializedName("timecreated") val timeCreated: Long? = 0,
    @SerializedName("timemodified") val timeModified: Long? = 0,
    @SerializedName("mimetype") val mimetype: String? = ""
)