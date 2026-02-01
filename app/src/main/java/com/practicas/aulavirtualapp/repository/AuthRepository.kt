package com.practicas.aulavirtualapp.repository

import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call

class AuthRepository {

    fun login(user: String, pass: String): Call<TokenResponse> {
        return RetrofitClient.instance.login(user, pass)
    }
}