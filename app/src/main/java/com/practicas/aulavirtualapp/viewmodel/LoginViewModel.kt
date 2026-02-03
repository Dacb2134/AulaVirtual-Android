package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.SiteInfoResponse // Asegúrate de importar esto
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

        // 1. PRIMER PASO: Pedir la "Llave" (Token)
        repository.login(user, pass).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                // OJO: No detenemos el 'cargando' todavía.

                if (response.isSuccessful && response.body()?.token != null) {
                    val tokenData = response.body()!!

                    // 🛑 ¡ALTO! Antes de terminar, vamos por el ID.
                    obtenerIdUsuario(tokenData)

                } else {
                    cargando.value = false
                    error.value = response.body()?.error ?: "Credenciales incorrectas"
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    // 2. SEGUNDO PASO (Interno): Pedir el "Carnet" (ID) usando la llave
    private fun obtenerIdUsuario(tokenData: TokenResponse) {
        repository.getSiteInfo(tokenData.token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                cargando.value = false // ¡AHORA SÍ terminamos!

                if (response.isSuccessful && response.body() != null) {
                    // ✅ Rellenamos el dato que faltaba en el objeto original
                    // (Asegúrate de haber agregado 'var userid: Int = 0' en TokenResponse como hicimos antes)
                    tokenData.userid = response.body()!!.userid

                    // 🎉 ¡Exito total! Entregamos el paquete completo (Token + ID)
                    resultadoLogin.value = tokenData
                } else {
                    error.value = "Login incompleto: No se pudo obtener el ID de usuario."
                }
            }

            override fun onFailure(call: Call<SiteInfoResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Fallo al identificar usuario: ${t.message}"
            }
        })
    }
}