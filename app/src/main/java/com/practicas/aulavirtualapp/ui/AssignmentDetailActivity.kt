package com.practicas.aulavirtualapp.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentDetailActivity : AppCompatActivity() {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_detail)

        val header = findViewById<android.view.View>(R.id.viewAssignmentHeader)
        val btnBack = findViewById<ImageButton>(R.id.btnAssignmentBack)
        val tvTitle = findViewById<TextView>(R.id.tvAssignmentTitle)
        val tvCourse = findViewById<TextView>(R.id.tvAssignmentCourse)
        val tvStatus = findViewById<TextView>(R.id.tvAssignmentStatus)
        val tvDueDate = findViewById<TextView>(R.id.tvAssignmentDueDate)
        val tvAvailability = findViewById<TextView>(R.id.tvAssignmentAvailability)
        val tvAttempts = findViewById<TextView>(R.id.tvAssignmentAttempts)
        val tvDescription = findViewById<TextView>(R.id.tvAssignmentDescription)
        val btnOpenMoodle = findViewById<android.view.View>(R.id.btnAssignmentOpenMoodle)

        val title = intent.getStringExtra(EXTRA_ASSIGNMENT_TITLE).orEmpty()
        val description = intent.getStringExtra(EXTRA_ASSIGNMENT_DESCRIPTION).orEmpty()
        val courseName = intent.getStringExtra(EXTRA_ASSIGNMENT_COURSE).orEmpty()
        val courseColor = intent.getIntExtra(EXTRA_ASSIGNMENT_COURSE_COLOR, 0)
        val dueDate = intent.getLongExtra(EXTRA_ASSIGNMENT_DUE_DATE, 0L)
        val allowFrom = intent.getLongExtra(EXTRA_ASSIGNMENT_ALLOW_FROM, 0L)
        val cutoffDate = intent.getLongExtra(EXTRA_ASSIGNMENT_CUTOFF, 0L)
        val gradingDueDate = intent.getLongExtra(EXTRA_ASSIGNMENT_GRADING_DUE, 0L)
        val maxAttempts = intent.getIntExtra(EXTRA_ASSIGNMENT_MAX_ATTEMPTS, 0)
        val courseModuleId = intent.getIntExtra(EXTRA_ASSIGNMENT_COURSE_MODULE_ID, 0)

        if (courseColor != 0) {
            header.setBackgroundColor(courseColor)
        }

        tvTitle.text = if (title.isNotBlank()) title else "Tarea"
        tvCourse.text = if (courseName.isNotBlank()) courseName else "Curso"
        tvDescription.text = if (description.isNotBlank()) {
            HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            "No hay descripción disponible."
        }

        val statusText = buildStatus(dueDate, allowFrom, cutoffDate)
        tvStatus.text = statusText
        tvStatus.setTextColor(statusColor(statusText))

        tvDueDate.text = buildDateLabel("Vence", dueDate)
        tvAvailability.text = buildAvailabilityLabel(allowFrom, cutoffDate, gradingDueDate)
        tvAttempts.text = if (maxAttempts > 0) "Intentos: $maxAttempts" else "Intentos: Sin límite"

        btnBack.setOnClickListener { finish() }
        btnOpenMoodle.setOnClickListener {
            if (courseModuleId == 0) {
                Toast.makeText(this, "Moodle no envió el enlace de esta tarea.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val url = "${RetrofitClient.baseUrl}mod/assign/view.php?id=$courseModuleId"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun buildStatus(dueDate: Long, allowFrom: Long, cutoffDate: Long): String {
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

    private fun buildDateLabel(label: String, timestampSeconds: Long): String {
        return if (timestampSeconds > 0) {
            val date = Date(timestampSeconds * 1000)
            "$label: ${dateTimeFormat.format(date)}"
        } else {
            "$label: Sin fecha"
        }
    }

    private fun buildAvailabilityLabel(allowFrom: Long, cutoffDate: Long, gradingDueDate: Long): String {
        val allowText = buildDateLabel("Disponible desde", allowFrom)
        val cutoffText = buildDateLabel("Cierra", cutoffDate)
        val gradingText = if (gradingDueDate > 0) {
            buildDateLabel("Calificación hasta", gradingDueDate)
        } else {
            "Calificación hasta: No especificado"
        }
        return "$allowText · $cutoffText · $gradingText"
    }

    private fun statusColor(status: String): Int {
        return when (status) {
            "Aún no disponible" -> Color.parseColor("#FF6D00")
            "Entrega cerrada", "Entrega fuera de plazo" -> Color.parseColor("#D32F2F")
            else -> Color.parseColor("#2E7D32")
        }
    }

    companion object {
        private const val EXTRA_ASSIGNMENT_TITLE = "extra_assignment_title"
        private const val EXTRA_ASSIGNMENT_DESCRIPTION = "extra_assignment_description"
        private const val EXTRA_ASSIGNMENT_COURSE = "extra_assignment_course"
        private const val EXTRA_ASSIGNMENT_COURSE_COLOR = "extra_assignment_course_color"
        private const val EXTRA_ASSIGNMENT_DUE_DATE = "extra_assignment_due_date"
        private const val EXTRA_ASSIGNMENT_ALLOW_FROM = "extra_assignment_allow_from"
        private const val EXTRA_ASSIGNMENT_CUTOFF = "extra_assignment_cutoff"
        private const val EXTRA_ASSIGNMENT_GRADING_DUE = "extra_assignment_grading_due"
        private const val EXTRA_ASSIGNMENT_MAX_ATTEMPTS = "extra_assignment_max_attempts"
        private const val EXTRA_ASSIGNMENT_COURSE_MODULE_ID = "extra_assignment_course_module_id"

        fun createIntent(
            context: Context,
            assignment: Assignment,
            fallbackCourseName: String,
            fallbackCourseColor: Int
        ): Intent {
            val intent = Intent(context, AssignmentDetailActivity::class.java)
            val courseName = if (assignment.courseName.isNotBlank()) assignment.courseName else fallbackCourseName
            val colorInt = if (fallbackCourseColor != 0) {
                fallbackCourseColor
            } else {
                try {
                    Color.parseColor(assignment.courseColor)
                } catch (_: IllegalArgumentException) {
                    0
                }
            }

            intent.putExtra(EXTRA_ASSIGNMENT_TITLE, assignment.name)
            intent.putExtra(EXTRA_ASSIGNMENT_DESCRIPTION, assignment.description.orEmpty())
            intent.putExtra(EXTRA_ASSIGNMENT_COURSE, courseName)
            intent.putExtra(EXTRA_ASSIGNMENT_COURSE_COLOR, colorInt)
            intent.putExtra(EXTRA_ASSIGNMENT_DUE_DATE, assignment.dueDate ?: 0L)
            intent.putExtra(EXTRA_ASSIGNMENT_ALLOW_FROM, assignment.allowSubmissionsFromDate ?: 0L)
            intent.putExtra(EXTRA_ASSIGNMENT_CUTOFF, assignment.cutoffDate ?: 0L)
            intent.putExtra(EXTRA_ASSIGNMENT_GRADING_DUE, assignment.gradingDueDate ?: 0L)
            intent.putExtra(EXTRA_ASSIGNMENT_MAX_ATTEMPTS, assignment.maxAttempts ?: 0)
            intent.putExtra(EXTRA_ASSIGNMENT_COURSE_MODULE_ID, assignment.courseModuleId ?: 0)
            return intent
        }
    }
}
