package com.practicas.aulavirtualapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.model.AssignmentConfig
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.repository.AuthRepository
import com.practicas.aulavirtualapp.ui.assignmentdetail.AssignmentDetailArgs
import com.practicas.aulavirtualapp.ui.assignmentdetail.AssignmentDetailFormatter
import com.practicas.aulavirtualapp.ui.assignmentdetail.AssignmentSubmissionManager
import com.practicas.aulavirtualapp.utils.AssignmentProgressStore
import java.text.SimpleDateFormat
import java.util.Locale

class AssignmentDetailActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private var assignmentId: Int = 0
    private var allowFileSubmission: Boolean = true
    private var allowTextSubmission: Boolean = true
    private var fileExtensions: String = ""
    private var userToken: String = ""
    private val authRepository = AuthRepository()
    private val formatter = AssignmentDetailFormatter(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")))
    private val submissionManager by lazy {
        AssignmentSubmissionManager(this, contentResolver, authRepository)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Necesitamos permiso para leer tus archivos.", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedFileUri = uri
            findViewById<TextView>(R.id.tvAssignmentSelectedFile).text = fileName(uri)
        }
    }

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
        val btnAttachFile = findViewById<MaterialButton>(R.id.btnAssignmentAttachFile)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnAssignmentSubmit)
        val btnMarkComplete = findViewById<MaterialButton>(R.id.btnAssignmentMarkComplete)
        val etText = findViewById<TextInputEditText>(R.id.etAssignmentText)
        val tilText = findViewById<TextInputLayout>(R.id.tilAssignmentText)
        val tvDeliveryHint = findViewById<TextView>(R.id.tvAssignmentDeliveryHint)
        val tvAllowedFormats = findViewById<TextView>(R.id.tvAssignmentAllowedFormats)

        val args = AssignmentDetailArgs.fromIntent(intent)
        assignmentId = args.assignmentId
        allowFileSubmission = args.allowFileSubmission
        allowTextSubmission = args.allowTextSubmission
        fileExtensions = args.fileExtensions
        userToken = args.userToken

        if (args.courseColor != 0) {
            header.setBackgroundColor(args.courseColor)
        }

        tvTitle.text = if (args.title.isNotBlank()) args.title else "Tarea"
        tvCourse.text = if (args.courseName.isNotBlank()) args.courseName else "Curso"
        tvDescription.text = if (args.description.isNotBlank()) {
            HtmlCompat.fromHtml(args.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            "No hay descripción disponible."
        }

        val statusText = formatter.buildStatus(args.dueDate, args.allowFrom, args.cutoffDate)
        tvStatus.text = statusText
        tvStatus.setTextColor(formatter.statusColor(statusText))

        tvDueDate.text = formatter.buildDateLabel("Vence", args.dueDate)
        tvAvailability.text = formatter.buildAvailabilityLabel(args.allowFrom, args.cutoffDate, args.gradingDueDate)
        tvAttempts.text = if (args.maxAttempts > 0) "Intentos: ${args.maxAttempts}" else "Intentos: Sin límite"

        btnBack.setOnClickListener { finish() }
        btnOpenMoodle.setOnClickListener {
            if (args.courseModuleId == 0) {
                Toast.makeText(this, "Moodle no envió el enlace de esta tarea.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val url = "${RetrofitClient.baseUrl}mod/assign/view.php?id=${args.courseModuleId}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        configureSubmissionUi(
            allowFileSubmission,
            allowTextSubmission,
            fileExtensions,
            btnAttachFile,
            tvAllowedFormats,
            tvDeliveryHint,
            tilText
        )
        btnAttachFile.setOnClickListener { requestFilePermission() }

        btnSubmit.setOnClickListener {
            val hasText = !etText.text.isNullOrBlank()
            val hasFile = selectedFileUri != null
            if (!allowTextSubmission && hasText) {
                Toast.makeText(this, "Esta tarea no permite entrega en texto.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!allowFileSubmission && hasFile) {
                Toast.makeText(this, "Esta tarea no permite adjuntar archivos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!hasText && !hasFile) {
                Toast.makeText(this, "Agrega texto o un archivo para continuar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hasFile && selectedFileUri != null) {
                val localFileName = fileName(selectedFileUri!!)
                if (!formatter.isFileExtensionAllowed(localFileName, fileExtensions)) {
                    Toast.makeText(
                        this,
                        "El archivo no cumple con los formatos permitidos: ${formatter.buildAllowedFormatsLabel(fileExtensions)}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }
            if (userToken.isBlank()) {
                Toast.makeText(this, "No se encontró el token de Moodle.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            val onSuccess = {
                AssignmentProgressStore.setCompleted(this@AssignmentDetailActivity, assignmentId, true)
                updateCompleteButton(findViewById(R.id.btnAssignmentMarkComplete))
            }
            if (hasFile && selectedFileUri != null) {
                submissionManager.submitWithFile(
                    token = userToken,
                    assignmentId = assignmentId,
                    text = etText.text?.toString(),
                    fileUri = selectedFileUri!!,
                    onComplete = { btnSubmit.isEnabled = true },
                    onSuccess = onSuccess
                )
            } else {
                submissionManager.saveSubmission(
                    token = userToken,
                    assignmentId = assignmentId,
                    text = etText.text?.toString(),
                    fileManagerId = null,
                    onComplete = { btnSubmit.isEnabled = true },
                    onSuccess = onSuccess
                )
            }
        }

        updateCompleteButton(btnMarkComplete)
        btnMarkComplete.setOnClickListener {
            val isCompleted = AssignmentProgressStore.getCompleted(this).contains(assignmentId.toString())
            AssignmentProgressStore.setCompleted(this, assignmentId, !isCompleted)
            updateCompleteButton(btnMarkComplete)
        }
    }

    private fun requestFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker()
            return
        }

        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openFilePicker()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun openFilePicker() {
        val mimeTypes = formatter.resolveAllowedMimeTypes(fileExtensions)
        pickFileLauncher.launch(mimeTypes)
    }

    private fun fileName(uri: Uri): String = submissionManager.fileName(uri)

    private fun updateCompleteButton(button: MaterialButton) {
        val isCompleted = AssignmentProgressStore.getCompleted(this).contains(assignmentId.toString())
        if (isCompleted) {
            button.text = "Marcar como pendiente"
        } else {
            button.text = "Marcar como hecha"
        }
    }

    private fun configureSubmissionUi(
        allowFiles: Boolean,
        allowText: Boolean,
        extensions: String,
        btnAttachFile: MaterialButton,
        tvAllowedFormats: TextView,
        tvDeliveryHint: TextView,
        tilText: TextInputLayout
    ) {
        if (!allowFiles) {
            btnAttachFile.visibility = View.GONE
            tvAllowedFormats.visibility = View.GONE
            findViewById<TextView>(R.id.tvAssignmentSelectedFile).visibility = View.GONE
        } else {
            tvAllowedFormats.text = formatter.buildAllowedFormatsLabel(extensions)
        }

        tilText.visibility = if (allowText) View.VISIBLE else View.GONE

        tvDeliveryHint.text = when {
            allowFiles && allowText -> "Entrega archivos o texto según el formato solicitado."
            allowFiles -> "Entrega únicamente archivos."
            allowText -> "Entrega únicamente texto."
            else -> "Esta tarea no admite entregas en la app."
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
        private const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
        private const val EXTRA_ASSIGNMENT_ALLOW_FILES = "extra_assignment_allow_files"
        private const val EXTRA_ASSIGNMENT_ALLOW_TEXT = "extra_assignment_allow_text"
        private const val EXTRA_ASSIGNMENT_FILE_EXTENSIONS = "extra_assignment_file_extensions"
        private const val EXTRA_USER_TOKEN = "extra_user_token"

        fun createIntent(
            context: Context,
            assignment: Assignment,
            fallbackCourseName: String,
            fallbackCourseColor: Int,
            userToken: String
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
            intent.putExtra(EXTRA_ASSIGNMENT_ID, assignment.id)
            val submissionInfo = buildSubmissionInfo(assignment)
            intent.putExtra(EXTRA_ASSIGNMENT_ALLOW_FILES, submissionInfo.allowFiles)
            intent.putExtra(EXTRA_ASSIGNMENT_ALLOW_TEXT, submissionInfo.allowText)
            intent.putExtra(EXTRA_ASSIGNMENT_FILE_EXTENSIONS, submissionInfo.fileExtensions)
            intent.putExtra(EXTRA_USER_TOKEN, userToken)
            return intent
        }

        private data class SubmissionInfo(
            val allowFiles: Boolean,
            val allowText: Boolean,
            val fileExtensions: String
        )

        private fun buildSubmissionInfo(assignment: Assignment): SubmissionInfo {
            // Si configs está vacío, dejamos todo habilitado por seguridad (o podrías restringir).
            if (assignment.configs.isEmpty()) {
                return SubmissionInfo(allowFiles = true, allowText = true, fileExtensions = "")
            }

            // 🕵️‍♂️ Lógica Estricta:
            val allowFiles = isPluginEnabled(assignment.configs, "file")
            val allowText = isPluginEnabled(assignment.configs, "onlinetext")

            // 🚨 CAMBIO AQUÍ: Usamos "filetypeslist" porque así viene en tu JSON
            val fileExtensions = assignment.configs.firstOrNull {
                it.subtype == "assignsubmission" &&
                        it.plugin == "file" &&
                        it.name == "filetypeslist"
            }?.value.orEmpty()

            return SubmissionInfo(allowFiles, allowText, fileExtensions)
        }

        // 👇 FUNCIÓN CON LÓGICA ESTRICTA
        private fun isPluginEnabled(configs: List<AssignmentConfig>, plugin: String): Boolean {
            val config = configs.find {
                it.subtype == "assignsubmission" &&
                        it.plugin == plugin &&
                        it.name == "enabled"
            }
            // Solo si existe y es "1" devolvemos true. Si no existe (null), es false.
            return config?.value == "1"
        }
    }
}
