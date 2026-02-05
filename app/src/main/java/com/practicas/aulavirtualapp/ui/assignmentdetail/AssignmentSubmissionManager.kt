package com.practicas.aulavirtualapp.ui.assignmentdetail

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.practicas.aulavirtualapp.model.MoodleUploadFile
import com.practicas.aulavirtualapp.model.SaveSubmissionResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssignmentSubmissionManager(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val authRepository: AuthRepository
) {
    fun submitWithFile(
        token: String,
        assignmentId: Int,
        text: String?,
        fileUri: Uri,
        onComplete: () -> Unit,
        onSuccess: () -> Unit
    ) {
        val fileName = fileName(fileUri)

        android.util.Log.d("UPLOAD_DEBUG", "--------------------------------------------------")
        android.util.Log.d("UPLOAD_DEBUG", "1. INICIANDO SUBIDA DE ARCHIVO")
        android.util.Log.d("UPLOAD_DEBUG", "   -> Archivo: $fileName")
        android.util.Log.d("UPLOAD_DEBUG", "   -> URI: $fileUri")

        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }

        if (fileBytes == null) {
            android.util.Log.e("UPLOAD_DEBUG", "   -> ERROR CRÍTICO: No se pudieron leer los bytes del archivo.")
            Toast.makeText(context, "No se pudo leer el archivo seleccionado.", Toast.LENGTH_LONG).show()
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
                            context,
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
                            context,
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
                        onComplete = onComplete,
                        onSuccess = onSuccess
                    )
                }

                override fun onFailure(call: Call<List<MoodleUploadFile>>, t: Throwable) {
                    android.util.Log.e("UPLOAD_DEBUG", "CRASH EN UPLOAD: ${t.message}")
                    Toast.makeText(
                        context,
                        "Error al subir archivo: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete()
                }
            })
    }

    fun saveSubmission(
        token: String,
        assignmentId: Int,
        text: String?,
        fileManagerId: Int?,
        onComplete: () -> Unit,
        onSuccess: () -> Unit
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
                            context,
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

                    val esExitoso = (body?.status == true) || warningsList.isEmpty()

                    if (!esExitoso) {
                        android.util.Log.e("UPLOAD_DEBUG", "   -> FALLO: Status no es true y hay warnings.")
                        Toast.makeText(
                            context,
                            "No se pudo guardar: $warnings",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    if (warnings.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "Enviado con avisos: $warnings",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Entrega enviada a Moodle correctamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    onSuccess()
                }

                override fun onFailure(call: Call<SaveSubmissionResponse>, t: Throwable) {
                    onComplete()
                    android.util.Log.e("UPLOAD_DEBUG", "CRASH EN SAVE: ${t.message}")
                    Toast.makeText(
                        context,
                        "Error al enviar entrega: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    fun fileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "Archivo adjunto"
    }
}
