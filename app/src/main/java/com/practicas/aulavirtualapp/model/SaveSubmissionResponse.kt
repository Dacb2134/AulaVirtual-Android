package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class SaveSubmissionResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("warnings") val warnings: List<MoodleWarning> = emptyList()
)

data class MoodleWarning(
    @SerializedName("warningcode") val warningCode: String? = null,
    @SerializedName("message") val message: String? = null
)
