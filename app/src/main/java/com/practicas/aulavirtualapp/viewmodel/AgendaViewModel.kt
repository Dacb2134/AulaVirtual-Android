package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.ColorGenerator
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.model.AssignmentResponse
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger

class AgendaViewModel : ViewModel() {

    private val repository = AuthRepository()

    // 1. Guardamos la lista MAESTRA
    private var listaCompleta: List<Assignment> = emptyList()

    val agenda = MutableLiveData<List<Assignment>>()
    val mensaje = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()

    // --- CARGA DE DATOS ---

    fun cargarAgendaGlobal(token: String, userId: Int) {
        cargando.value = true

        Log.e("AGENDA_DEBUG", "==================================================")
        Log.d("AGENDA_DEBUG", "1. INICIANDO CARGA GLOBAL (UserID: $userId)")

        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful && response.body() != null) {
                    val cursos = response.body()!!
                    Log.d("AGENDA_DEBUG", "2. CURSOS RECIBIDOS: ${cursos.size}")

                    //  SINCRONIZACIÓN DE COLORES

                    cursos.forEachIndexed { index, curso ->
                        curso.color = ColorGenerator.getIconColorHex(index)
                        Log.d("AGENDA_DEBUG", "   -> Curso: [ID: ${curso.id}] ${curso.fullName} | Color: ${curso.color}")
                    }

                    traerTareasDeTodosLosCursos(token, cursos)
                } else {
                    Log.e("AGENDA_DEBUG", "ERROR AL OBTENER CURSOS: ${response.code()}")
                    cargando.value = false
                    mensaje.value = "Error al obtener cursos"
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "FALLO DE RED (CURSOS): ${t.message}")
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

        Log.d("AGENDA_DEBUG", "3. SOLICITANDO TAREAS A ${cursos.size} CURSOS...")

        for (curso in cursos) {
            repository.getAssignments(token, curso.id).enqueue(object : Callback<AssignmentResponse> {
                override fun onResponse(call: Call<AssignmentResponse>, response: Response<AssignmentResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        // Buscamos el curso por ID o tomamos el primero
                        val cursoData = body?.courses?.find { it.id == curso.id }
                            ?: body?.courses?.firstOrNull()

                        val tareasEncontradas = cursoData?.assignments ?: emptyList()

                        Log.v("AGENDA_DEBUG", "   -> Curso [ID ${curso.id}]: ${tareasEncontradas.size} tareas.")

                        tareasEncontradas.forEach { tarea ->
                            // 🛠️ CORRECCIÓN AQUÍ TAMBIÉN: fullName
                            tarea.courseName = curso.fullName ?: "Curso sin nombre"
                            tarea.courseColor = curso.color ?: "#6200EE"
                            listaTotalTareas.add(tarea)
                        }
                    } else {
                        Log.e("AGENDA_DEBUG", "   -> ERROR HTTP Curso ${curso.id}: ${response.code()}")
                    }

                    if (cursosPendientes.decrementAndGet() == 0) {
                        finalizarCarga(listaTotalTareas)
                    }
                }

                override fun onFailure(call: Call<AssignmentResponse>, t: Throwable) {
                    Log.e("AGENDA_DEBUG", "   -> FALLO RED Curso ${curso.id}: ${t.message}")
                    if (cursosPendientes.decrementAndGet() == 0) {
                        finalizarCarga(listaTotalTareas)
                    }
                }
            })
        }
    }

    private fun finalizarCarga(tareas: MutableList<Assignment>) {
        cargando.value = false
        Log.d("AGENDA_DEBUG", "4. CARGA FINALIZADA. Total: ${tareas.size}")

        listaCompleta = tareas.sortedBy { it.dueDate ?: 0L }

        if (listaCompleta.isEmpty()) {
            Log.w("AGENDA_DEBUG", "   -> La lista final quedó vacía.")
            mensaje.value = "¡Todo al día! No hay tareas."
            agenda.value = emptyList()
        } else {
            filtrarTodas()
        }
    }

    // --- FILTROS ---
    fun filtrarTodas() { agenda.value = listaCompleta }

    fun filtrarProximos7Dias() {
        val hoy = System.currentTimeMillis()
        val sieteDias = hoy + (7L * 24 * 60 * 60 * 1000)
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
            (it.dueDate ?: 0L) > 0 && fechaVencimiento < hoy
        }
        agenda.value = filtradas
    }
}