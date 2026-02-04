package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseContentViewModel : ViewModel() {
    private val repository = AuthRepository()

    val sections = MutableLiveData<List<CourseSection>>()
    val message = MutableLiveData<String>()

    private var lastCourseId: Int? = null

    fun loadCourseContents(token: String, courseId: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && lastCourseId == courseId && sections.value != null) return
        lastCourseId = courseId

        repository.getCourseContents(token, courseId).enqueue(object : Callback<List<CourseSection>> {
            override fun onResponse(
                call: Call<List<CourseSection>>,
                response: Response<List<CourseSection>>
            ) {
                if (response.isSuccessful) {
                    sections.value = response.body() ?: emptyList()
                } else {
                    Log.e("MI_APP", "Error cargando contenido: ${response.code()}")
                    message.value = "Error al cargar contenido: ${response.code()}"
                    sections.value = emptyList()
                }
            }

            override fun onFailure(call: Call<List<CourseSection>>, t: Throwable) {
                Log.e("MI_APP", "Fallo de conexión: ${t.message}")
                message.value = "Fallo de conexión: ${t.message}"
                sections.value = emptyList()
            }
        })
    }
}
