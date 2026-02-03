package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgendaViewModel : ViewModel() {

    private val repository = AuthRepository()

    val agenda = MutableLiveData<List<Assignment>>()
    val mensaje = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()

    fun cargarAgendaGlobal(token: String, userId: Int) {
        cargando.value = true

        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful && response.body() != null) {
                    val cursos = response.body()!!

                    // --- NUEVO BLOQUE: Asignar colores aleatorios fijos ---
                    val colores = listOf("#FF5722", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#FF9800")
                    cursos.forEachIndexed { index, curso ->
                        // Usamos el residuo (%) para repartir los colores cíclicamente
                        curso.color = colores[index % colores.size]
                    }
                    // -----------------------------------------------------

                    traerTareasDeTodosLosCursos(token, cursos)
                } else {
                    cargando.value = false
                    mensaje.value = "Error al obtener cursos"
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                cargando.value = false
                mensaje.value = "Error de red: ${t.message}"
            }
        })
    }

    private fun traerTareasDeTodosLosCursos(token: String, cursos: List<Course>) {
        val listaTotalTareas = mutableListOf<Assignment>()
        var respuestasRecibidas = 0

        if (cursos.isEmpty()) {
            cargando.value = false
            mensaje.value = "No tienes cursos inscritos"
            return
        }

        // 2. Pedimos las tareas de CADA curso
        for (curso in cursos) {
            repository.getAssignments(token, curso.id).enqueue(object : Callback<AssignmentResponse> {
                override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {

                    response.body()?.courses?.forEach { cursoMoodle ->
                        cursoMoodle.assignments.forEach { tarea ->
                            // AQUÍ ESTÁ EL TRUCO: Le pegamos la etiqueta del curso a la tarea
                            tarea.courseName = curso.fullName
                            tarea.courseColor = curso.color ?: "#6200EE" // Si no tiene color, usa morado
                            listaTotalTareas.add(tarea)
                        }
                    }
                    verificarSiTerminamos(cursos.size, ++respuestasRecibidas, listaTotalTareas)
                }

                override fun onFailure(call: Call<AssignmentResponse>, t: Throwable) {
                    verificarSiTerminamos(cursos.size, ++respuestasRecibidas, listaTotalTareas)
                }
            })
        }
    }

    // Función auxiliar para saber cuándo dejar de cargar
    private fun verificarSiTerminamos(totalCursos: Int, procesados: Int, tareas: MutableList<Assignment>) {
        if (procesados == totalCursos) {
            cargando.value = false
            if (tareas.isEmpty()) {
                mensaje.value = "¡Todo al día! No hay tareas pendientes."
            } else {
                // Ordenamos: Primero lo más urgente (menor fecha)
                agenda.value = tareas.sortedBy { it.dueDate }
            }
        }
    }
}