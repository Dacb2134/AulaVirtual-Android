package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.SiteInfoResponse // <--- Importante que use el nuevo nombre
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val repository = AuthRepository()

    val cursos = MutableLiveData<List<Course>>()
    val mensaje = MutableLiveData<String>()

    // Función para cargar TODO (Saludo + Cursos)
    fun cargarDatosUsuario(token: String) {

        // 1. Pedimos info del usuario (Para el saludo)
        repository.getSiteInfo(token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!

                    // 👇 CORRECCIÓN AQUÍ:
                    // Antes buscabas .fullName, ahora es .fullname (minúscula, como en el modelo)
                    mensaje.value = "Hola ${usuario.fullname}."

                    // Una vez sabemos quién es, pedimos sus cursos usando su ID
                    // Antes era .userId, ahora es .userid
                    cargarCursos(token, usuario.userid)
                } else {
                    mensaje.value = "Error al identificar usuario"
                }
            }

            override fun onFailure(call: Call<SiteInfoResponse>, t: Throwable) {
                mensaje.value = "Fallo de conexión: ${t.message}"
            }
        })
    }

    private fun cargarCursos(token: String, userId: Int) {
        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful) {
                    cursos.value = response.body() ?: emptyList()
                } else {
                    mensaje.value = "Error al cargar cursos"
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                mensaje.value = "Error: ${t.message}"
            }
        })
    }
}