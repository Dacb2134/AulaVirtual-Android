package com.practicas.aulavirtualapp.viewmodel

import android.util.Log // 👈 Importante: Esto nos permite imprimir mensajes en la consola
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDetailViewModel : ViewModel() {
    private val repository = AuthRepository()

    // LiveData para avisar a la pantalla cuando lleguen las tareas
    val assignments = MutableLiveData<List<Assignment>>()
    val message = MutableLiveData<String>()

    private var lastCourseId: Int? = null

    fun loadAssignments(token: String, courseId: Int) {
        if (lastCourseId == courseId && assignments.value != null) return
        lastCourseId = courseId

        Log.d("MI_APP", "Pidiendo tareas a Moodle... Token: $token, CourseID: $courseId")

        // Llamamos a Moodle
        repository.getAssignments(token, courseId).enqueue(object : Callback<AssignmentResponse> {
            override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {
                if (response.isSuccessful) {
                    val respuestaMoodle = response.body()

                    // 🕵️‍♂️ LOGS ESPIAS: Busca "MI_APP" en el Logcat para ver esto
                    Log.d("MI_APP", "Respuesta recibida: $respuestaMoodle")

                    // Buscamos si Moodle nos devolvió datos para este curso específico
                    val cursoEncontrado = respuestaMoodle?.courses?.find { it.courseId == courseId }

                    if (cursoEncontrado != null) {
                        Log.d("MI_APP", "¡Curso encontrado! Tiene ${cursoEncontrado.assignments.size} tareas.")

                        if (cursoEncontrado.assignments.isNotEmpty()) {
                            assignments.value = cursoEncontrado.assignments
                        } else {
                            // Si la lista está vacía
                            assignments.value = emptyList()
                            message.value = "No hay tareas pendientes (Lista vacía)"
                        }
                    } else {
                        Log.d("MI_APP", "El curso ID $courseId no vino en la respuesta de Moodle.")
                        assignments.value = emptyList()
                        message.value = "No hay tareas pendientes 🎉"
                    }
                } else {
                    Log.e("MI_APP", "Error del servidor: Código ${response.code()}")
                    message.value = "Error al cargar tareas: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<AssignmentResponse>, t: Throwable) {
                Log.e("MI_APP", "Fallo de conexión crítico: ${t.message}")
                message.value = "Fallo de conexión: ${t.message}"
            }
        })
    }
}
