package com.practicas.aulavirtualapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.LoginResult
import com.practicas.aulavirtualapp.model.OAuthTokenResponse
import com.practicas.aulavirtualapp.model.SiteInfoResponse
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

    // token de Administrador
    private val MASTER_TOKEN = "89178c549fdf4b2268e338271c1161a4"

    val resultadoLogin = MutableLiveData<LoginResult?>()
    val error = MutableLiveData<String>()
    val cargando = MutableLiveData<Boolean>()

    //LOGIN MANUAL
    fun realizarLogin(user: String, pass: String) {
        cargando.value = true
        repository.login(user, pass).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body()?.token != null) {
                    val tokenData = response.body()!!
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

    // LOGIN OAUTH ANTIGUO (prueba no usado--)
    fun realizarLoginOAuth(authCode: String, redirectUri: String) {
        cargando.value = true
        repository.loginWithOAuth(authCode, redirectUri).enqueue(object : Callback<OAuthTokenResponse> {
            override fun onResponse(call: Call<OAuthTokenResponse>, response: Response<OAuthTokenResponse>) {
                cargando.value = false
                error.value = "Método OAuth antiguo no soportado. Usando flujo directo."
            }
            override fun onFailure(call: Call<OAuthTokenResponse>, t: Throwable) {
                cargando.value = false
                error.value = "Error: ${t.message}"
            }
        })
    }

    // LOGIN CON GOOGLE

    fun realizarLoginGoogle(email: String, displayName: String) {
        cargando.value = true

        // 1. Buscamos si existe en Moodle usando el Token Maestro
        repository.checkUserExists(MASTER_TOKEN, email).enqueue(object : Callback<List<UserDetail>> {
            override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                val users = response.body()

                if (response.isSuccessful && !users.isNullOrEmpty()) {
                    // CASO A: YA EXISTE -> Entramos directo
                    finalizarLoginExitoso(users[0])
                } else {
                    // CASO B: NO EXISTE -> Lo creamos (Auto-registro)
                    crearUsuarioEnMoodle(email, displayName)
                }
            }
            override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
                cargando.value = false
                error.value = "Error conectando con Moodle: ${t.message}"
            }
        })
    }

    private fun crearUsuarioEnMoodle(email: String, displayName: String) {
        repository.registerUser(MASTER_TOKEN, email, displayName).enqueue(object : Callback<List<UserDetail>> {
            override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    // ¡CREADO! -> Entramos con el nuevo usuario
                    val nuevoUsuario = response.body()!![0]
                    finalizarLoginExitoso(nuevoUsuario)
                } else {
                    cargando.value = false
                    error.value = "No se pudo registrar. Verifique permisos 'create_users' en Moodle."
                }
            }
            override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
                cargando.value = false
                error.value = "Error al crear usuario: ${t.message}"
            }
        })
    }

    private fun finalizarLoginExitoso(usuario: UserDetail) {
        cargando.value = false

        // Lógica simple de roles: ID 2 es Admin, el resto Estudiantes.
        val rol = if (usuario.id == 2) UserRole.ADMIN else UserRole.ESTUDIANTE

        resultadoLogin.value = LoginResult(
            token = MASTER_TOKEN,
            userId = usuario.id,
            role = rol
        )
    }

    //  (Login Manual) ---
    private fun obtenerPerfilYRol(tokenData: TokenResponse) {
        repository.getSiteInfo(tokenData.token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val siteInfo = response.body()!!
                    tokenData.userid = siteInfo.userid
                    obtenerRolDesdeDetalles(tokenData, siteInfo)
                } else {
                    cargando.value = false
                    error.value = "Login incompleto: No se pudo obtener el ID."
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
                override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                    cargando.value = false
                    val userDetail = response.body()?.firstOrNull()
                    val role = UserRoleResolver.resolve(siteInfo, userDetail)
                    resultadoLogin.value = LoginResult(tokenData.token, tokenData.userid, role)
                }
                override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
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