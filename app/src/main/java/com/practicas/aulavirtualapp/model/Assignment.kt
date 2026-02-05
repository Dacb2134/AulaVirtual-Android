package com.practicas.aulavirtualapp.model

import com.google.gson.annotations.SerializedName

// 1. Respuesta General
data class AssignmentResponse(
    @SerializedName("courses") val courses: List<CourseAssignments> = emptyList()
)

// 2. El Contenedor del Curso (VOLVEMOS A TU ESTRUCTURA QUE FUNCIONABA)
data class CourseAssignments(
    // 🟢 CLAVE DEL ÉXITO: Moodle manda "id", nosotros lo guardamos como "id" (o courseId, pero usemos id para estandarizar)
    @SerializedName("id") val id: Int,
    @SerializedName("fullname") val fullname: String? = "",
    @SerializedName("shortname") val shortname: String? = "",
    @SerializedName("assignments") val assignments: List<Assignment> = emptyList()
)

// 3. Configuración (Necesaria para que no falle al leer detalles)
data class AssignmentConfig(
    @SerializedName("plugin") val plugin: String? = null,
    @SerializedName("subtype") val subtype: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: String? = null
)

// 4. La Tarea Individual (HÍBRIDO: Estructura vieja + Seguridad nueva)
data class Assignment(
    @SerializedName("id") val id: Int,
    @SerializedName("cmid") val courseModuleId: Int? = null,
    @SerializedName("name") val name: String,

    // 🟢 Mantenemos Long? (nullable) porque si Moodle manda null, tu app vieja crashearía. Esto es más seguro.
    @SerializedName("duedate") val dueDate: Long? = 0L,

    @SerializedName("intro") val description: String? = null,
    @SerializedName("grade") val grade: Int? = null,

    // Campos necesarios para Detalle (No los borres, los necesitas)
    @SerializedName("allowsubmissionsfromdate") val allowSubmissionsFromDate: Long? = null,
    @SerializedName("cutoffdate") val cutoffDate: Long? = null,
    @SerializedName("gradingduedate") val gradingDueDate: Long? = null,
    @SerializedName("maxattempts") val maxAttempts: Int? = null,
    @SerializedName("configs") val configs: List<AssignmentConfig> = emptyList(),

    // Variables extras para tu UI (Colores y Nombres)
    var courseName: String = "",
    var courseColor: String = "#6200EE"
)