package com.practicas.aulavirtualapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicas.aulavirtualapp.model.Badge
import com.practicas.aulavirtualapp.model.BadgeResponse
import com.practicas.aulavirtualapp.model.MoodleFile
import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.UserDetail
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _userDetail = MutableLiveData<UserDetail>()
    val userDetail: LiveData<UserDetail> get() = _userDetail

    // LiveData para el texto de la cinta (ADMIN, DOCENTE, ESTUDIANTE)
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole

    private val _badges = MutableLiveData<List<Badge>>()
    val badges: LiveData<List<Badge>> get() = _badges

    private val _files = MutableLiveData<List<MoodleFile>>()
    val files: LiveData<List<MoodleFile>> get() = _files

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // Variable temporal para guardar si es admin mientras carga el resto del perfil
    private var isSiteAdmin = false

    fun cargarPerfilCompleto(token: String, userId: Int) {
        _cargando.value = true

        // 1. PRIMER CHEQUEO: ¿Es Super Admin? (Fuente: getSiteInfo)
        repository.getSiteInfo(token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    isSiteAdmin = response.body()!!.isSiteAdmin

                    if (isSiteAdmin) {
                        _userRole.value = "ADMIN"
                        // Si es admin, tiene prioridad sobre cualquier otro rol.
                    }
                }
            }
            override fun onFailure(call: Call<SiteInfoResponse>, t: Throwable) {
                // Si falla la red, no asumimos nada todavía
            }
        })

        // 2. SEGUNDO CHEQUEO: Detalles del usuario (Lista de usuarios)
        repository.getUserDetails(token, userId).enqueue(object : Callback<List<UserDetail>> {
            override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!! // Recibimos la lista directa [ ... ]

                    if (lista.isNotEmpty()) {
                        val user = lista[0] // Tomamos el primer usuario
                        _userDetail.value = user // Actualizamos la UI con los datos

                        // Si NO es Admin global, verificamos si es Docente
                        if (!isSiteAdmin) {
                            procesarRoles(user)
                        }
                    } else {
                        Log.e("Profile", "La lista de usuarios llegó vacía")
                    }
                } else {
                    Log.e("Profile", "Error en respuesta: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
                _cargando.value = false
                Log.e("Profile", "Fallo de red: ${t.message}")
            }
        })

        // 3. CARGA DE EXTRAS (Medallas y Archivos)
        cargarExtras(token, userId)
    }

    // Lógica inteligente: Combina Roles de API + Atributo de Departamento
    private fun procesarRoles(user: UserDetail) {
        val roles = user.roles ?: emptyList()

        // A. Buscamos por ROL TÉCNICO
        val tieneRolDocente = roles.any { rol ->
            rol.shortName == "editingteacher" ||
                    rol.shortName == "teacher" ||
                    rol.shortName == "coursecreator" ||
                    rol.shortName == "manager"
        }

        // B. Buscamos por DEPARTAMENTO (Tu "Plan B" seguro)
        // Detecta si pusiste "Docente" en el campo departamento de Moodle
        val esDepartamentoDocente = user.department?.contains("Docente", ignoreCase = true) == true

        // --- DECISIÓN FINAL ---
        if (tieneRolDocente || esDepartamentoDocente) {
            _userRole.value = "DOCENTE"
        } else {
            _userRole.value = "ESTUDIANTE"
        }
    }

    private fun cargarExtras(token: String, userId: Int) {
        repository.getUserBadges(token, userId).enqueue(object : Callback<BadgeResponse> {
            override fun onResponse(call: Call<BadgeResponse>, response: Response<BadgeResponse>) {
                if (response.isSuccessful) _badges.value = response.body()?.badges ?: emptyList()
            }
            override fun onFailure(call: Call<BadgeResponse>, t: Throwable) {}
        })

        repository.getUserFiles(token, userId).enqueue(object : Callback<List<MoodleFile>> {
            override fun onResponse(call: Call<List<MoodleFile>>, response: Response<List<MoodleFile>>) {
                _cargando.value = false
                if (response.isSuccessful) _files.value = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<MoodleFile>>, t: Throwable) {
                _cargando.value = false
            }
        })
    }
}