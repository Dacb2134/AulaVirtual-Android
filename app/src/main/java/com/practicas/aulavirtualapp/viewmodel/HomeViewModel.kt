package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.UserDetail
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val repository = AuthRepository()

    val cursos = MutableLiveData<List<Course>>()
    val mensaje = MutableLiveData<String>()

    fun cargarDatosUsuario(token: String, userId: Int) {

        repository.getUserDetails(token, userId).enqueue(object : Callback<List<UserDetail>> {
            override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val usuario = response.body()!![0]
                    mensaje.value = "Hola ${usuario.fullname}."

                    cargarCursos(token, userId)
                } else {
                    mensaje.value = "Error al identificar usuario"
                }
            }

            override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
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