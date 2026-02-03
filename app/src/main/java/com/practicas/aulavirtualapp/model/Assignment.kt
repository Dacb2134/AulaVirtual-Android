package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

// La respuesta general de Moodle
data class AssignmentResponse(
    @SerializedName("courses") val courses: List<CourseAssignments>
)

// El contenedor por curso
data class CourseAssignments(
    @SerializedName("id") val courseId: Int,
    @SerializedName("assignments") val assignments: List<Assignment>
)

// La Tarea individual (ACTUALIZADA)
data class Assignment(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("duedate") val dueDate: Long, // Fecha límite (timestamp)
    @SerializedName("intro") val description: String? = null,


    // Agregamos estos campos para guardar el nombre y color del curso.
    var courseName: String = "",
    var courseColor: String = "#6200EE"
)