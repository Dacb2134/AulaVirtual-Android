package com.practicas.aulavirtualapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Vistas nuevas
        val rvCourses = findViewById<RecyclerView>(R.id.rvCourses)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting) // Nuevo ID

        rvCourses.layoutManager = LinearLayoutManager(this)
        // Inicializamos el adaptador con la ACCIÓN DE CLIC
        adapter = CourseAdapter(emptyList()) { course, color ->
            // Esto se ejecuta cuando el usuario toca una tarjeta
            val intent = android.content.Intent(this, CourseDetailActivity::class.java)
            intent.putExtra("COURSE_ID", course.id)
            intent.putExtra("COURSE_NAME", course.fullName)
            intent.putExtra("COURSE_COLOR", color) // Pasamos el color para que combine
            startActivity(intent)
        }
        rvCourses.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val token = intent.getStringExtra("USER_TOKEN")
        if (token != null) {
            pbLoading.visibility = View.VISIBLE
            viewModel.cargarDatosUsuario(token)
        }

        // Observamos el mensaje para actualizar el saludo
        viewModel.mensaje.observe(this) { texto ->
            if (texto.contains("Hola")) {
                tvGreeting.text = texto.split(".")[0] + " 👋"
            } else if (texto.contains("Error") || texto.contains("Fallo")) {
                pbLoading.visibility = View.GONE
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.cursos.observe(this) { listaCursos ->
            pbLoading.visibility = View.GONE
            if (listaCursos.isNotEmpty()) {
                adapter.updateData(listaCursos)
            } else {
                tvGreeting.text = "Sin cursos asignados"
            }
        }
    }
}