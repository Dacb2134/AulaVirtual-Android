package com.practicas.aulavirtualapp.ui.assignmentdetail

import android.graphics.Color
import android.webkit.MimeTypeMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentDetailFormatter(
    private val dateTimeFormat: SimpleDateFormat
) {
    fun buildStatus(dueDate: Long, allowFrom: Long, cutoffDate: Long): String {
        val now = System.currentTimeMillis()
        val dueMillis = dueDate * 1000
        val allowMillis = allowFrom * 1000
        val cutoffMillis = cutoffDate * 1000

        return when {
            allowFrom > 0 && now < allowMillis -> "Aún no disponible"
            cutoffDate > 0 && now > cutoffMillis -> "Entrega cerrada"
            dueDate > 0 && now > dueMillis -> "Entrega fuera de plazo"
            else -> "Disponible para entregar"
        }
    }

    fun statusColor(status: String): Int {
        return when (status) {
            "Aún no disponible" -> Color.parseColor("#FF6D00")
            "Entrega cerrada", "Entrega fuera de plazo" -> Color.parseColor("#D32F2F")
            else -> Color.parseColor("#2E7D32")
        }
    }

    fun buildDateLabel(label: String, timestampSeconds: Long): String {
        return if (timestampSeconds > 0) {
            val date = Date(timestampSeconds * 1000)
            "$label: ${dateTimeFormat.format(date)}"
        } else {
            "$label: Sin fecha"
        }
    }

    fun buildAvailabilityLabel(allowFrom: Long, cutoffDate: Long, gradingDueDate: Long): String {
        val allowText = buildDateLabel("Disponible desde", allowFrom)
        val cutoffText = buildDateLabel("Cierra", cutoffDate)
        val gradingText = if (gradingDueDate > 0) {
            buildDateLabel("Calificación hasta", gradingDueDate)
        } else {
            "Calificación hasta: No especificado"
        }
        return "$allowText · $cutoffText · $gradingText"
    }

    fun buildAllowedFormatsLabel(extensions: String): String {
        val cleaned = extensions.split(",")
            .map { it.trim().trimStart('.') }
            .filter { it.isNotBlank() }
            .map { it.uppercase(Locale.getDefault()) }

        return if (cleaned.isEmpty()) {
            "Formatos permitidos: Sin restricción"
        } else {
            "Formatos permitidos: ${cleaned.joinToString(", ")}"
        }
    }

    fun resolveAllowedMimeTypes(extensions: String): Array<String> {
        val cleaned = extensions.split(",")
            .map { it.trim().trimStart('.').lowercase(Locale.getDefault()) }
            .filter { it.isNotBlank() }
        if (cleaned.isEmpty()) {
            return arrayOf("*/*")
        }
        val mimeTypes = cleaned.mapNotNull { ext ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        }.distinct()
        return if (mimeTypes.isEmpty()) arrayOf("*/*") else mimeTypes.toTypedArray()
    }

    fun isFileExtensionAllowed(fileName: String, extensions: String): Boolean {
        val allowed = extensions.split(",")
            .map { it.trim().trimStart('.').lowercase(Locale.getDefault()) }
            .filter { it.isNotBlank() }
        if (allowed.isEmpty()) {
            return true
        }
        val fileExtension = fileName.substringAfterLast('.', "").lowercase(Locale.getDefault())
        return fileExtension.isNotBlank() && allowed.contains(fileExtension)
    }
}
