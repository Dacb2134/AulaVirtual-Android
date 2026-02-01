package com.practicas.aulavirtualapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.practicas.aulavirtualapp.network.RetrofitClient
import com.practicas.aulavirtualapp.network.TokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Escribe usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Conectando..."

            // 🚀 LLAMADA A MOODLE
            RetrofitClient.instance.login(user, pass).enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val datos = response.body()
                        if (datos?.token != null) {
                            // ¡EXITO TOTAL!
                            Log.d("LOGIN", "Token: ${datos.token}")
                            tvStatus.text = "¡Login Correcto!\nToken: ${datos.token}"
                            Toast.makeText(applicationContext, "Bienvenido $user", Toast.LENGTH_LONG).show()
                        } else {
                            tvStatus.text = "Error: ${datos?.error ?: "Credenciales incorrectas"}"
                        }
                    } else {
                        tvStatus.text = "Error del Servidor: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    tvStatus.text = "Fallo de red: ${t.message}"
                }
            })
        }
    }
}