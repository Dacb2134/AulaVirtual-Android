package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.GradeItem
import com.practicas.aulavirtualapp.model.GradeReportResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseGradesViewModel : ViewModel() {
    private val repository = AuthRepository()

    val grades = MutableLiveData<List<GradeItem>>()
    val message = MutableLiveData<String>()

    fun loadGrades(token: String, courseId: Int, userId: Int) {
        repository.getGradeReport(token, courseId, userId).enqueue(object : Callback<GradeReportResponse> {
            override fun onResponse(
                call: Call<GradeReportResponse>,
                response: Response<GradeReportResponse>
            ) {
                if (response.isSuccessful) {
                    val gradeItems = response.body()?.userGrades?.firstOrNull()?.gradeItems ?: emptyList()
                    grades.value = gradeItems
                } else {
                    Log.e("MI_APP", "Error cargando notas: ${response.code()}")
                    message.value = "Error al cargar notas: ${response.code()}"
                    grades.value = emptyList()
                }
            }

            override fun onFailure(call: Call<GradeReportResponse>, t: Throwable) {
                Log.e("MI_APP", "Fallo de conexión notas: ${t.message}")
                message.value = "Fallo de conexión: ${t.message}"
                grades.value = emptyList()
            }
        })
    }
}
