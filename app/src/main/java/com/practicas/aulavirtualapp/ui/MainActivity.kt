package com.practicas.aulavirtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.practicas.aulavirtualapp.ui.HomeActivity
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Conectamos con el ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // OBSERVAMOS los cambios

        viewModel.cargando.observe(this) { estaCargando ->
            if (estaCargando) {
                tvStatus.text = "Conectando con Moodle..."
                btnLogin.isEnabled = false
            } else {
                btnLogin.isEnabled = true
            }
        }


        viewModel.resultadoLogin.observe(this) { response ->
            if (response != null) {
                tvStatus.text = "¡Login Correcto!"
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()


                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USER_TOKEN", response.token) // Le pasamos la llave
                startActivity(intent)
                finish()
            }
        }


        viewModel.error.observe(this) { mensajeError ->
            tvStatus.text = "Error: $mensajeError"
            Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
        }

        //  El botón solo le da la orden al ViewModel
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