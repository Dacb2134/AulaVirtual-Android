package com.practicas.aulavirtualapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.practicas.aulavirtualapp.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvInfo = findViewById<TextView>(R.id.tvTokenInfo)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        // Conectar con el ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Recibir el token
        val token = intent.getStringExtra("USER_TOKEN")

        if (token != null) {

            viewModel.cargarDatosUsuario(token)
        }

        // Observar mensajes de estado
        viewModel.mensaje.observe(this) { texto ->
            tvInfo.text = texto
        }

        // Observar la lista de cursos
        viewModel.cursos.observe(this) { listaCursos ->
            // Cuando lleguen los cursos, los mostramos en pantalla
            var resultado = "TUS CURSOS ENCONTRADOS:\n\n"
            for (curso in listaCursos) {
                resultado += "📚 ${curso.fullName}\n\n"
            }
            tvInfo.text = resultado
            tvWelcome.text = "¡Carga Completa!"
        }
    }
}