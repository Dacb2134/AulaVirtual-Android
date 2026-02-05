package com.practicas.aulavirtualapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.webkit.MimeTypeMap
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
import com.practicas.aulavirtualapp.model.MoodleUploadFile
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.repository.AuthRepository
import com.practicas.aulavirtualapp.utils.AssignmentProgressStore
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentDetailActivity : AppCompatActivity() {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    private var selectedFileUri: Uri? = null
    private var assignmentId: Int = 0
    private var allowFileSubmission: Boolean = true
    private var allowTextSubmission: Boolean = true
    private var fileExtensions: String = ""
    private var userToken: String = ""
    private val authRepository = AuthRepository()

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
        assignmentId = intent.getIntExtra(EXTRA_ASSIGNMENT_ID, 0)
        allowFileSubmission = intent.getBooleanExtra(EXTRA_ASSIGNMENT_ALLOW_FILES, true)
        allowTextSubmission = intent.getBooleanExtra(EXTRA_ASSIGNMENT_ALLOW_TEXT, true)
        fileExtensions = intent.getStringExtra(EXTRA_ASSIGNMENT_FILE_EXTENSIONS).orEmpty()
        userToken = intent.getStringExtra(EXTRA_USER_TOKEN).orEmpty()

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
                if (!isFileExtensionAllowed(localFileName, fileExtensions)) {
                    Toast.makeText(
                        this,
                        "El archivo no cumple con los formatos permitidos: ${buildAllowedFormatsLabel(fileExtensions)}",
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
            if (hasFile && selectedFileUri != null) {
                submitWithFile(
                    token = userToken,
                    assignmentId = assignmentId,
                    text = etText.text?.toString(),
                    fileUri = selectedFileUri!!,
                    onComplete = { btnSubmit.isEnabled = true }
                )
            } else {
                saveSubmission(
                    token = userToken,
                    assignmentId = assignmentId,
                    text = etText.text?.toString(),
                    fileManagerId = null,
                    onComplete = { btnSubmit.isEnabled = true }
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
        val mimeTypes = resolveAllowedMimeTypes(fileExtensions)
        pickFileLauncher.launch(mimeTypes)
    }

    private fun fileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "Archivo adjunto"
    }

    private fun updateCompleteButton(button: MaterialButton) {
        val isCompleted = AssignmentProgressStore.getCompleted(this).contains(assignmentId.toString())
        if (isCompleted) {
            button.text = "Marcar como pendiente"
        } else {
            button.text = "Marcar como hecha"
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
            tvAllowedFormats.text = buildAllowedFormatsLabel(extensions)
        }

        tilText.visibility = if (allowText) View.VISIBLE else View.GONE

        tvDeliveryHint.text = when {
            allowFiles && allowText -> "Entrega archivos o texto según el formato solicitado."
            allowFiles -> "Entrega únicamente archivos."
            allowText -> "Entrega únicamente texto."
            else -> "Esta tarea no admite entregas en la app."
        }
    }

    private fun buildAllowedFormatsLabel(extensions: String): String {
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

    private fun resolveAllowedMimeTypes(extensions: String): Array<String> {
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

    private fun isFileExtensionAllowed(fileName: String, extensions: String): Boolean {
        val allowed = extensions.split(",")
            .map { it.trim().trimStart('.').lowercase(Locale.getDefault()) }
            .filter { it.isNotBlank() }
        if (allowed.isEmpty()) {
            return true
        }
        val fileExtension = fileName.substringAfterLast('.', "").lowercase(Locale.getDefault())
        return fileExtension.isNotBlank() && allowed.contains(fileExtension)
    }

    private fun submitWithFile(
        token: String,
        assignmentId: Int,
        text: String?,
        fileUri: Uri,
        onComplete: () -> Unit
    ) {
        val fileName = fileName(fileUri)

        // 🕵️‍♂️ LOG INICIO
        android.util.Log.d("UPLOAD_DEBUG", "--------------------------------------------------")
        android.util.Log.d("UPLOAD_DEBUG", "1. INICIANDO SUBIDA DE ARCHIVO")
        android.util.Log.d("UPLOAD_DEBUG", "   -> Archivo: $fileName")
        android.util.Log.d("UPLOAD_DEBUG", "   -> URI: $fileUri")

        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }

        if (fileBytes == null) {
            android.util.Log.e("UPLOAD_DEBUG", "   -> ERROR CRÍTICO: No se pudieron leer los bytes del archivo.")
            Toast.makeText(this, "No se pudo leer el archivo seleccionado.", Toast.LENGTH_LONG).show()
            onComplete()
            return
        } else {
            android.util.Log.d("UPLOAD_DEBUG", "   -> Archivo leído en memoria. Tamaño: ${fileBytes.size} bytes")
        }

        val textMediaType = "text/plain".toMediaType()
        val fileMediaType = mimeType.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType()
        val tokenBody = token.toRequestBody(textMediaType)
        val filePathBody = "/".toRequestBody(textMediaType)
        val itemIdBody = "0".toRequestBody(textMediaType)
        val fileBody = fileBytes.toRequestBody(fileMediaType)
        val filePart = MultipartBody.Part.createFormData("file", fileName, fileBody)

        authRepository.uploadAssignmentFile(tokenBody, filePathBody, itemIdBody, filePart)
            .enqueue(object : Callback<List<MoodleUploadFile>> {
                override fun onResponse(
                    call: Call<List<MoodleUploadFile>>,
                    response: Response<List<MoodleUploadFile>>
                ) {
                    android.util.Log.d("UPLOAD_DEBUG", "2. RESPUESTA UPLOAD RECIBIDA (Código: ${response.code()})")

                    if (!response.isSuccessful) {
                        android.util.Log.e("UPLOAD_DEBUG", "   -> ERROR HTTP UPLOAD: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "Error al subir el archivo (${response.code()}).",
                            Toast.LENGTH_LONG
                        ).show()
                        onComplete()
                        return
                    }

                    val uploaded = response.body().orEmpty()
                    android.util.Log.d("UPLOAD_DEBUG", "   -> JSON Upload: $uploaded")

                    val itemId = uploaded.firstOrNull()?.itemId
                    android.util.Log.d("UPLOAD_DEBUG", "   -> ITEMID OBTENIDO: $itemId")

                    if (itemId == null || itemId == 0) {
                        android.util.Log.e("UPLOAD_DEBUG", "   -> ERROR: El itemId es nulo o 0. Moodle no guardó el borrador.")
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "Moodle no devolvió el itemid del archivo.",
                            Toast.LENGTH_LONG
                        ).show()
                        onComplete()
                        return
                    }

                    saveSubmission(
                        token = token,
                        assignmentId = assignmentId,
                        text = text,
                        fileManagerId = itemId,
                        onComplete = onComplete
                    )
                }

                override fun onFailure(call: Call<List<MoodleUploadFile>>, t: Throwable) {
                    android.util.Log.e("UPLOAD_DEBUG", "CRASH EN UPLOAD: ${t.message}")
                    Toast.makeText(
                        this@AssignmentDetailActivity,
                        "Error al subir archivo: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete()
                }
            })
    }

    private fun saveSubmission(
        token: String,
        assignmentId: Int,
        text: String?,
        fileManagerId: Int?,
        onComplete: () -> Unit
    ) {
        android.util.Log.d("UPLOAD_DEBUG", "3. LLAMANDO A SAVE SUBMISSION")
        android.util.Log.d("UPLOAD_DEBUG", "   -> Assignment ID: $assignmentId")
        android.util.Log.d("UPLOAD_DEBUG", "   -> FileManager ID (Draft): $fileManagerId")
        android.util.Log.d("UPLOAD_DEBUG", "   -> Texto: $text")

        authRepository.saveAssignmentSubmission(token, assignmentId, text, fileManagerId)
            .enqueue(object : Callback<SaveSubmissionResponse> {
                override fun onResponse(
                    call: Call<SaveSubmissionResponse>,
                    response: Response<SaveSubmissionResponse>
                ) {
                    onComplete()
                    android.util.Log.d("UPLOAD_DEBUG", "4. RESPUESTA SAVE SUBMISSION (Código: ${response.code()})")

                    if (!response.isSuccessful) {
                        android.util.Log.e("UPLOAD_DEBUG", "   -> ERROR HTTP SAVE: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "Error al enviar la entrega (${response.code()}).",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val body = response.body()
                    android.util.Log.d("UPLOAD_DEBUG", "   -> BODY COMPLETO: $body")

                    val warningsList = body?.warnings ?: emptyList()
                    val warnings = warningsList.joinToString(" · ") { it.message.orEmpty() }

                    android.util.Log.d("UPLOAD_DEBUG", "   -> WARNINGS: $warnings")

                    // 🏆 FIX APLICADO: Si status es true O si no hay warnings (y el código es 200), asumimos éxito.
                    val esExitoso = (body?.status == true) || warningsList.isEmpty()

                    if (!esExitoso) {
                        android.util.Log.e("UPLOAD_DEBUG", "   -> FALLO: Status no es true y hay warnings.")
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "No se pudo guardar: $warnings",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    if (warnings.isNotEmpty()) {
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "Enviado con avisos: $warnings",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AssignmentDetailActivity,
                            "Entrega enviada a Moodle correctamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    AssignmentProgressStore.setCompleted(this@AssignmentDetailActivity, assignmentId, true)
                    updateCompleteButton(findViewById(R.id.btnAssignmentMarkComplete))
                }

                override fun onFailure(call: Call<SaveSubmissionResponse>, t: Throwable) {
                    onComplete()
                    android.util.Log.e("UPLOAD_DEBUG", "CRASH EN SAVE: ${t.message}")
                    Toast.makeText(
                        this@AssignmentDetailActivity,
                        "Error al enviar entrega: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
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