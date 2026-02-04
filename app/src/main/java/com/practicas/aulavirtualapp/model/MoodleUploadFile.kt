package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class MoodleUploadFile(
    @SerializedName("filename") val fileName: String,
    @SerializedName("filepath") val filePath: String? = "",
    @SerializedName("filesize") val fileSize: Int? = 0,
    @SerializedName("fileurl") val fileUrl: String? = "",
    @SerializedName("itemid") val itemId: Int? = 0,
    @SerializedName("mimetype") val mimeType: String? = ""
)
