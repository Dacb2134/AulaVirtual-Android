package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

data class SubmissionStatusResponse(
    @SerializedName("lastattempt")
    val lastAttempt: LastAttempt? = null
)

data class LastAttempt(
    @SerializedName("submission")
    val submission: MoodleSubmission? = null
)

data class MoodleSubmission(
    @SerializedName("id")
    val id: Int,
    @SerializedName("status")
    val status: String?
)