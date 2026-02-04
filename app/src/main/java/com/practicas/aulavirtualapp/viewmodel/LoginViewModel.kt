package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.LoginResult
import com.practicas.aulavirtualapp.model.OAuthTokenResponse
import com.practicas.aulavirtualapp.model.SiteInfoResponse // Asegúrate de importar esto
import com.practicas.aulavirtualapp.model.UserDetail
import com.practicas.aulavirtualapp.model.UserRole
import com.practicas.aulavirtualapp.network.TokenResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import com.practicas.aulavirtualapp.utils.UserRoleResolver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    val resultadoLogin = MutableLiveData<LoginResult?>()
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

                    // 🛑 ¡ALTO! Antes de terminar, vamos por el perfil y rol.
                    obtenerPerfilYRol(tokenData)

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
    fun realizarLoginOAuth(authCode: String, redirectUri: String) {
        cargando.value = true

        repository.loginWithOAuth(authCode, redirectUri).enqueue(object : Callback<OAuthTokenResponse> {
            override fun onResponse(
                call: Call<OAuthTokenResponse>,
                response: Response<OAuthTokenResponse>
            ) {
                val oauthResponse = response.body()
                if (response.isSuccessful && oauthResponse?.accessToken != null) {
                    val tokenData = TokenResponse(token = oauthResponse.accessToken)
                    obtenerPerfilYRol(tokenData)
                } else {
                    cargando.value = false
                    error.value = oauthResponse?.errorDescription ?: "No se pudo validar Google OAuth."
                }
            }

            override fun onFailure(call: Call<OAuthTokenResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Error de OAuth: ${t.message}"
            }
        })
    }

    private fun obtenerPerfilYRol(tokenData: TokenResponse) {
        repository.getSiteInfo(tokenData.token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // ✅ Rellenamos el dato que faltaba en el objeto original
                    // (Asegúrate de haber agregado 'var userid: Int = 0' en TokenResponse como hicimos antes)
                    val siteInfo = response.body()!!
                    tokenData.userid = siteInfo.userid
                    obtenerRolDesdeDetalles(tokenData, siteInfo)
                } else {
                    cargando.value = false
                    error.value = "Login incompleto: No se pudo obtener el ID de usuario."
                }
            }

            override fun onFailure(call: Call<SiteInfoResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Fallo al identificar usuario: ${t.message}"
            }
        })
    }

    private fun obtenerRolDesdeDetalles(tokenData: TokenResponse, siteInfo: SiteInfoResponse) {
        repository.getUserDetails(tokenData.token, tokenData.userid)
            .enqueue(object : Callback<List<UserDetail>> {
                override fun onResponse(
                    call: Call<List<UserDetail>>,
                    response: Response<List<UserDetail>>
                ) {
                    cargando.value = false
                    val userDetail = response.body()?.firstOrNull()
                    val role = UserRoleResolver.resolve(siteInfo, userDetail)
                    resultadoLogin.value = LoginResult(
                        token = tokenData.token,
                        userId = tokenData.userid,
                        role = role
                    )
                }

                override fun onFailure(
                    call: Call<List<com.practicas.aulavirtualapp.model.UserDetail>>,
                    t: Throwable
                ) {
                    cargando.value = false
                    resultadoLogin.value = LoginResult(
                        token = tokenData.token,
                        userId = tokenData.userid,
                        role = if (siteInfo.isSiteAdmin) UserRole.ADMIN else UserRole.ESTUDIANTE
                    )
                }
            })
    }
}
