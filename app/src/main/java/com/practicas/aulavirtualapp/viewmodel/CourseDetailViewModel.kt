package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
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

    // LiveData UI
    val assignments = MutableLiveData<List<Assignment>>(emptyList())
    val message = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>(false) // Necesario para el Zorro

    private var lastCourseId: Int? = null

    fun loadAssignments(token: String, courseId: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && lastCourseId == courseId && assignments.value?.isNotEmpty() == true) return

        lastCourseId = courseId
        isLoading.value = true // Mostrar Zorro

        Log.d("MI_APP", "Solicitando tareas... CourseID: $courseId")

        repository.getAssignments(token, courseId).enqueue(object : Callback<AssignmentResponse> {
            override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {
                isLoading.value = false // Ocultar Zorro

                if (response.isSuccessful) {
                    val body = response.body()
                    val cursos = body?.courses.orEmpty()

                    // ESTRATEGIA SEGURA:
                    // 1. Buscamos por ID exacto
                    // 2. Si no, tomamos el primero que venga (porque Moodle suele devolver solo el que pediste)
                    val cursoEncontrado = cursos.find { it.id == courseId } ?: cursos.firstOrNull()

                    val listaTareas = cursoEncontrado?.assignments ?: emptyList()

                    if (listaTareas.isNotEmpty()) {
                        Log.d("MI_APP", "¡Éxito! Tareas cargadas: ${listaTareas.size}")
                        assignments.value = listaTareas.sortedBy { it.dueDate ?: 0L }
                    } else {
                        Log.d("MI_APP", "El curso vino sin tareas.")
                        assignments.value = emptyList()
                    }
                } else {
                    Log.e("MI_APP", "Error HTTP: ${response.code()} ${response.errorBody()?.string().orEmpty()}")
                    message.value = "Error al cargar: ${response.code()}"
                    assignments.value = emptyList()
                }
            }

            override fun onFailure(call: Call<AssignmentResponse>, t: Throwable) {
                isLoading.value = false
                Log.e("MI_APP", "Error red: ${t.message}")
                message.value = "Fallo de conexión"
                assignments.value = emptyList()
            }
        })
    }
}
