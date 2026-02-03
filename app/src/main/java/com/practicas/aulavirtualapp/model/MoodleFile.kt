package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class FileResponse(
    @SerializedName("files") val files: List<MoodleFile>
)

data class MoodleFile(
    @SerializedName("filename") val fileName: String,
    @SerializedName("filepath") val filePath: String,
    @SerializedName("filesize") val fileSize: Int,
    @SerializedName("url") val fileUrl: String,
    @SerializedName("timecreated") val timeCreated: Long,
    @SerializedName("timemodified") val timeModified: Long
)