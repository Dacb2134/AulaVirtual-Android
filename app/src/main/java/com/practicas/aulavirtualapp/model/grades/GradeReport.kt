package com.practicas.aulavirtualapp.model.grades

import com.google.gson.annotations.SerializedName

data class GradeReportResponse(
    @SerializedName("usergrades") val userGrades: List<UserGrade> = emptyList()
)

data class UserGrade(
    @SerializedName("courseid") val courseId: Int,
    @SerializedName("userid") val userId: Int,
    @SerializedName("userfullname") val userFullName: String?,
    @SerializedName("gradeitems") val gradeItems: List<GradeItem> = emptyList()
)

data class GradeItem(
    @SerializedName("id") val id: Int,
    @SerializedName("itemname") val itemName: String?,
    @SerializedName("itemtype") val itemType: String?,
    @SerializedName("graderaw") val gradeRaw: Double?,
    @SerializedName("gradeformatted") val gradeFormatted: String?,
    @SerializedName("grademin") val gradeMin: Double?,
    @SerializedName("grademax") val gradeMax: Double?,
    @SerializedName("percentageformatted") val percentageFormatted: String?,
    @SerializedName("weightformatted") val weightFormatted: String?
)
