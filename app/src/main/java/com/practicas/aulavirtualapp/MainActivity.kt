package com.practicas.aulavirtualapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.practicas.aulavirtualapp.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)


        viewModel.cargando.observe(this) { estaCargando ->
            if (estaCargando) {
                tvStatus.text = "Conectando con Moodle..."
                btnLogin.isEnabled = false // Desactivar botón para que no den doble clic
            } else {
                btnLogin.isEnabled = true
            }
        }


        viewModel.resultadoLogin.observe(this) { response ->
            if (response != null) {
                tvStatus.text = "¡Login Correcto!\nToken: ${response.token}"
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
                // Aquí en el futuro abriremos la siguiente pantalla
            }
        }


        viewModel.error.observe(this) { mensajeError ->
            tvStatus.text = "Error: $mensajeError"
            Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
        }


        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.realizarLogin(user, pass)
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}