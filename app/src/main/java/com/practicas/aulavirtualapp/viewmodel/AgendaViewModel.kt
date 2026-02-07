package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.ColorGenerator
import com.practicas.aulavirtualapp.model.assignment.Assignment
import com.practicas.aulavirtualapp.model.assignment.AssignmentResponse
import com.practicas.aulavirtualapp.model.course.Course
import com.practicas.aulavirtualapp.repository.AuthRepository
import com.practicas.aulavirtualapp.model.assignment.SubmissionStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger


// Clase para agrupar los totales de los filtros
data class AgendaCounts(val all: Int, val next7Days: Int, val overdue: Int)

class AgendaViewModel : ViewModel() {

    private val repository = AuthRepository()
    private var listaCompleta: List<Assignment> = emptyList()
    private var completedIds: Set<String> = emptySet()

    val agenda = MutableLiveData<List<Assignment>>()
    val mensaje = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()
    val conteos = MutableLiveData<AgendaCounts>()

    fun cargarAgendaGlobal(token: String, userId: Int, completedIds: Set<String>) {
        this.completedIds = completedIds
        cargando.value = true

        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful && response.body() != null) {
                    val cursos = response.body()!!
                    cursos.forEachIndexed { index, curso ->
                        curso.color = ColorGenerator.getIconColorHex(index)
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
        val cursosPendientes = AtomicInteger(cursos.size)

        if (cursos.isEmpty()) {
            cargando.value = false
            mensaje.value = "No tienes cursos inscritos"
            return
        }

        for (curso in cursos) {
            repository.getAssignments(token, curso.id).enqueue(object : Callback<AssignmentResponse> {
                override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {
                    if (response.isSuccessful) {
                        val cursoData = response.body()?.courses?.find { it.id == curso.id }
                            ?: response.body()?.courses?.firstOrNull()

                        val tareasMoodle = cursoData?.assignments ?: emptyList()

                        tareasMoodle.forEach { tarea ->
                            tarea.courseName = curso.fullName ?: "Curso sin nombre"
                            tarea.courseColor = curso.color ?: "#6200EE"

                            // 🕵️‍♂️ Análisis de Sincronización Real
                            val estaHechaLocal = completedIds.contains(tarea.id.toString())

                            if (estaHechaLocal) {
                                // Si la app cree que está hecha, verificamos con Moodle su estado real
                                repository.getSubmissionStatus(token, tarea.id).enqueue(object : Callback<SubmissionStatusResponse> {
                                    override fun onResponse(call: Call<SubmissionStatusResponse>, res: Response<SubmissionStatusResponse>) {
                                        val status = res.body()?.lastAttempt?.submission?.status

                                        // Si status es null o "new", el profesor borró la entrega
                                        if (status == null || status == "new") {
                                            // 🛠️ ACTUALIZACIÓN DE CACHÉ: Ya no está hecha
                                            // Aquí necesitarás pasar el context al ViewModel o manejar un evento para limpiar el store
                                            listaTotalTareas.add(tarea) // La volvemos a meter a la lista de pendientes
                                        }

                                        if (cursosPendientes.decrementAndGet() == 0) finalizarCarga(listaTotalTareas)
                                    }
                                    override fun onFailure(call: Call<SubmissionStatusResponse>, t: Throwable) {
                                        if (cursosPendientes.decrementAndGet() == 0) finalizarCarga(listaTotalTareas)
                                    }
                                })
                            } else {
                                // Si no está hecha localmente, va directo a la lista de la Agenda
                                listaTotalTareas.add(tarea)
                                if (cursosPendientes.decrementAndGet() == 0) finalizarCarga(listaTotalTareas)
                            }
                        }
                    } else {
                        if (cursosPendientes.decrementAndGet() == 0) finalizarCarga(listaTotalTareas)
                    }
                }
                override fun onFailure(call: Call<AssignmentResponse>, t: Throwable) {
                    if (cursosPendientes.decrementAndGet() == 0) finalizarCarga(listaTotalTareas)
                }
            })
        }
    }

    private fun finalizarCarga(tareas: MutableList<Assignment>) {
        cargando.value = false
        listaCompleta = tareas.sortedBy { it.dueDate ?: 0L }

        // 📈 Cálculo de Conteos
        val hoy = System.currentTimeMillis()
        val sieteDias = hoy + (7L * 24 * 60 * 60 * 1000)

        val total = listaCompleta.size
        val proximos = listaCompleta.count { (it.dueDate ?: 0L) * 1000 in hoy..sieteDias }
        val atrasadas = listaCompleta.count { (it.dueDate ?: 0L) > 0 && (it.dueDate ?: 0L) * 1000 < hoy }

        conteos.value = AgendaCounts(total, proximos, atrasadas)

        if (listaCompleta.isEmpty()) {
            mensaje.value = "¡Todo al día! No hay tareas pendientes."
            agenda.value = emptyList()
        } else {
            filtrarTodas()
        }
    }

    fun filtrarTodas() { agenda.value = listaCompleta }

    fun filtrarProximos7Dias() {
        val hoy = System.currentTimeMillis()
        val sieteDias = hoy + (7L * 24 * 60 * 60 * 1000)
        agenda.value = listaCompleta.filter { (it.dueDate ?: 0L) * 1000 in hoy..sieteDias }
    }

    fun filtrarAtrasadas() {
        val hoy = System.currentTimeMillis()
        agenda.value = listaCompleta.filter { (it.dueDate ?: 0L) > 0 && (it.dueDate ?: 0L) * 1000 < hoy }
    }
}