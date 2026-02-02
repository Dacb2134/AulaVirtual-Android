package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Course
import com.practicas.aulavirtualapp.model.SiteInfo
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val repository = AuthRepository()


    val cursos = MutableLiveData<List<Course>>()
    val mensaje = MutableLiveData<String>()

    fun cargarDatosUsuario(token: String) {
        mensaje.value = "Buscando tu ID de usuario..."

        // Pedimos la info del usuario (SiteInfo)
        repository.getSiteInfo(token).enqueue(object : Callback<SiteInfo> {
            override fun onResponse(call: Call<SiteInfo>, response: Response<SiteInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    val userId = response.body()!!.userId
                    val nombre = response.body()!!.fullName
                    mensaje.value = "Hola $nombre. Buscando tus cursos..."

                    cargarCursos(token, userId)
                } else {
                    mensaje.value = "Error identificando usuario"
                }
            }

            override fun onFailure(call: Call<SiteInfo>, t: Throwable) {
                mensaje.value = "Fallo de red: ${t.message}"
            }
        })
    }

    private fun cargarCursos(token: String, userId: Int) {
        repository.getUserCourses(token, userId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful && response.body() != null) {
                    // ¡BINGO! Llegaron los cursos
                    cursos.value = response.body()
                } else {
                    mensaje.value = "No se encontraron cursos"
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                mensaje.value = "Error cargando cursos: ${t.message}"
            }
        })
    }
}