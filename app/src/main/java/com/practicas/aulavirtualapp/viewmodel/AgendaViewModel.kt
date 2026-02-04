package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
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

    // 1. Guardamos la lista MAESTRA (privada) para poder filtrar sin perder datos
    private var listaCompleta: List<Assignment> = emptyList()

    val agenda = MutableLiveData<List<Assignment>>()
    val mensaje = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()

    // --- LÓGICA DE FILTROS ---

    fun filtrarTodas() {
        agenda.value = listaCompleta
    }

    fun filtrarProximos7Dias() {
        val hoy = System.currentTimeMillis()
        val sieteDias = hoy + (7L * 24 * 60 * 60 * 1000) // 7 días en milisegundos

        val filtradas = listaCompleta.filter {
            val fechaVencimiento = (it.dueDate ?: 0L) * 1000
            fechaVencimiento in hoy..sieteDias
        }
        agenda.value = filtradas
    }

    fun filtrarAtrasadas() {
        val hoy = System.currentTimeMillis()
        val filtradas = listaCompleta.filter {
            val fechaVencimiento = (it.dueDate ?: 0L) * 1000
            fechaVencimiento < hoy // Ya pasó
        }
        agenda.value = filtradas
    }

    // --- PREPARACIÓN PARA NOTIFICACIONES PUSH (Futuro) ---
    private fun detectarTareasUrgentes(tareas: List<Assignment>) {
        val hoy = System.currentTimeMillis()
        val manana = hoy + (24 * 60 * 60 * 1000) // Próximas 24 horas

        val tareasUrgentes = tareas.filter {
            val vencimiento = (it.dueDate ?: 0L) * 1000
            vencimiento in hoy..manana
        }

        if (tareasUrgentes.isNotEmpty()) {
            // TODO: FUTURO -> Aquí llamaremos al WorkManager para lanzar la notificación
            Log.d("NOTIFICACIONES", "¡Ojo! Hay ${tareasUrgentes.size} tareas que vencen en menos de 24h.")
            // Ejemplo: NotificationHelper.mostrarAviso(tareasUrgentes.first())
        }
    }

    // --- CARGA DE DATOS ---

    fun cargarAgendaGlobal(token: String, userId: Int) {
        cargando.value = true

        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful && response.body() != null) {
                    val cursos = response.body()!!

                    val colores = listOf("#FF5722", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#FF9800")
                    cursos.forEachIndexed { index, curso ->
                        curso.color = colores[index % colores.size]
                    }

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

        for (curso in cursos) {
            repository.getAssignments(token, curso.id).enqueue(object : Callback<AssignmentResponse> {
                override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {
                    response.body()?.courses?.forEach { cursoMoodle ->
                        cursoMoodle.assignments.forEach { tarea ->
                            tarea.courseName = curso.fullName
                            tarea.courseColor = curso.color ?: "#6200EE"
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

    private fun verificarSiTerminamos(totalCursos: Int, procesados: Int, tareas: MutableList<Assignment>) {
        if (procesados == totalCursos) {
            cargando.value = false

            // Guardamos la LISTA MAESTRA ordenada
            listaCompleta = tareas.sortedBy { it.dueDate ?: 0L }

            //  Revisamos si hay tareas para notificar (Lógica futura)
            detectarTareasUrgentes(listaCompleta)

            if (listaCompleta.isEmpty()) {
                mensaje.value = "¡Todo al día! No hay tareas pendientes."
                agenda.value = emptyList()
            } else {
                // Por defecto mostramos TODAS al inicio
                filtrarTodas()
            }
        }
    }
}
