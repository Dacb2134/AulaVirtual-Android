package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.EnrolledUser
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseParticipantsViewModel : ViewModel() {
    private val repository = AuthRepository()

    val teachers = MutableLiveData<List<EnrolledUser>>()
    val message = MutableLiveData<String>()

    fun loadTeachers(token: String, courseId: Int) {
        repository.getEnrolledUsers(token, courseId).enqueue(object : Callback<List<EnrolledUser>> {
            override fun onResponse(
                call: Call<List<EnrolledUser>>,
                response: Response<List<EnrolledUser>>
            ) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val docentes = users.filter { user ->
                        user.roles.any { role ->
                            val shortName = role.shortName.orEmpty()
                            shortName == "editingteacher" || shortName == "teacher"
                        }
                    }
                    teachers.value = docentes
                } else {
                    Log.e("MI_APP", "Error cargando docentes: ${response.code()}")
                    message.value = "Error al cargar docentes: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<EnrolledUser>>, t: Throwable) {
                Log.e("MI_APP", "Fallo de conexión docentes: ${t.message}")
                message.value = "Fallo de conexión: ${t.message}"
            }
        })
    }
}
