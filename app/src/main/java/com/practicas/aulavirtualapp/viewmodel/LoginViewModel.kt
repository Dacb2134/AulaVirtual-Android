package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.network.TokenResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()


    val resultadoLogin = MutableLiveData<TokenResponse?>()
    val error = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()

    fun realizarLogin(user: String, pass: String) {
        cargando.value = true

        repository.login(user, pass).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                cargando.value = false
                if (response.isSuccessful && response.body()?.token != null) {
                    resultadoLogin.value = response.body()
                } else {
                    error.value = response.body()?.error ?: "Credenciales incorrectas"
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Error de conexión: ${t.message}"
            }
        })
    }
}