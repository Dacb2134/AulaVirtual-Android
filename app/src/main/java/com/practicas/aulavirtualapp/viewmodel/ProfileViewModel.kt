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

    // 👇 NUEVO: LiveData específico para el texto de la cinta
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole

    private val _badges = MutableLiveData<List<Badge>>()
    val badges: LiveData<List<Badge>> get() = _badges

    private val _files = MutableLiveData<List<MoodleFile>>()
    val files: LiveData<List<MoodleFile>> get() = _files

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    fun cargarPerfilCompleto(token: String, userId: Int) {
        _cargando.value = true

        // 1. DETERMINAR ROL (Admin vs Estudiante)
        // Usamos getSiteInfo porque tu prueba en Postman confirmó que ahí sale "userissiteadmin"
        repository.getSiteInfo(token).enqueue(object : Callback<SiteInfoResponse> {
            override fun onResponse(call: Call<SiteInfoResponse>, response: Response<SiteInfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val info = response.body()!!
                    if (info.isSiteAdmin) {
                        _userRole.value = "ADMIN"
                    } else {
                        _userRole.value = "ESTUDIANTE"
                    }
                } else {
                    _userRole.value = "ESTUDIANTE"
                }
            }
            override fun onFailure(call: Call<SiteInfoResponse>, t: Throwable) {
                _userRole.value = "ESTUDIANTE"
            }
        })

        // 2. OBTENER DETALLES DEL USUARIO
        repository.getUserDetails(token, userId).enqueue(object : Callback<List<UserDetail>> {
            override fun onResponse(call: Call<List<UserDetail>>, response: Response<List<UserDetail>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!
                    if (lista.isNotEmpty()) {
                        _userDetail.value = lista[0]
                    }
                }
            }
            override fun onFailure(call: Call<List<UserDetail>>, t: Throwable) {
                _cargando.value = false
            }
        })

        // 3. MEDALLAS
        repository.getUserBadges(token, userId).enqueue(object : Callback<BadgeResponse> {
            override fun onResponse(call: Call<BadgeResponse>, response: Response<BadgeResponse>) {
                if (response.isSuccessful) _badges.value = response.body()?.badges ?: emptyList()
            }
            override fun onFailure(call: Call<BadgeResponse>, t: Throwable) {}
        })

        // 4. ARCHIVOS (Finaliza la carga visual)
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