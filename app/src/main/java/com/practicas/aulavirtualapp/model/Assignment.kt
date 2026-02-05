package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

// ===============================
// RESPUESTA GENERAL DE MOODLE
// ===============================
data class AssignmentResponse(
    @SerializedName("courses")
    val courses: List<CourseAssignments> = emptyList()
)

// ===============================
// CONTENEDOR DEL CURSO
// ===============================
data class CourseAssignments(

    @SerializedName("id")
    val id: Int,

    @SerializedName("fullname")
    val fullname: String? = "",

    @SerializedName("shortname")
    val shortname: String? = "",

    @SerializedName("assignments")
    val assignments: List<Assignment> = emptyList()
)

// ===============================
// CONFIGURACIÓN DEL ASSIGNMENT
// ===============================
data class AssignmentConfig(

    @SerializedName("plugin")
    val plugin: String? = null,

    @SerializedName("subtype")
    val subtype: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("value")
    val value: String? = null
)

// ===============================
// ASSIGNMENT (TAREA)
// ===============================
data class Assignment(

    @SerializedName("id")
    val id: Int,

    @SerializedName("cmid")
    val courseModuleId: Int? = null,

    @SerializedName("name")
    val name: String,

    // Moodle a veces manda null → NO crashea
    @SerializedName("duedate")
    val dueDate: Long? = null,

    @SerializedName("intro")
    val description: String? = null,

    @SerializedName("grade")
    val grade: Int? = null,

    @SerializedName("allowsubmissionsfromdate")
    val allowSubmissionsFromDate: Long? = null,

    @SerializedName("cutoffdate")
    val cutoffDate: Long? = null,

    @SerializedName("gradingduedate")
    val gradingDueDate: Long? = null,

    @SerializedName("maxattempts")
    val maxAttempts: Int? = null,

    @SerializedName("configs")
    val configs: List<AssignmentConfig> = emptyList(),


    var courseName: String = "",
    var courseColor: String = "#6200EE"
)
