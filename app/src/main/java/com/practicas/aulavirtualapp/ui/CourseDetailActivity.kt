package com.practicas.aulavirtualapp.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter
import com.practicas.aulavirtualapp.viewmodel.CourseDetailViewModel

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: CourseDetailViewModel
    private lateinit var adapter: AssignmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // 1. Recibir datos del Intent
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Curso"
        val courseId = intent.getIntExtra("COURSE_ID", 0)
        val courseColor = intent.getIntExtra("COURSE_COLOR", 0)
        val token = intent.getStringExtra("USER_TOKEN")

        // 2. Referencias a la Vista
        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        val header = findViewById<View>(R.id.viewHeader)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading) // Aquí vive tu Zorro
        val rvAssignments = findViewById<RecyclerView>(R.id.rvAssignments)

        // 3. Configurar Diseño (Color y Título)
        tvTitle.text = courseName
        if (courseColor != 0) header.setBackgroundColor(courseColor)

        // 4. Configurar la Lista (RecyclerView)
        rvAssignments.layoutManager = LinearLayoutManager(this)
        adapter = AssignmentAdapter()
        rvAssignments.adapter = adapter

        // 5. Conectar con el ViewModel
        viewModel = ViewModelProvider(this)[CourseDetailViewModel::class.java]

        // 6. Lógica de Carga (AQUÍ ESTÁ EL CAMBIO DEL ZORRO)
        if (token != null && courseId != 0) {
            // Mostramos el Zorro
            pbLoading.visibility = View.VISIBLE
            // Ocultamos la lista para que el Zorro se vea limpio en el centro
            rvAssignments.visibility = View.GONE

            viewModel.loadAssignments(token, courseId)
        } else {
            Toast.makeText(this, "Error: Datos del curso incompletos", Toast.LENGTH_SHORT).show()
        }

        // 7. Observar respuestas
        viewModel.assignments.observe(this) { listaTareas ->
            pbLoading.visibility = View.GONE
            rvAssignments.visibility = View.VISIBLE
            adapter.updateData(listaTareas)
        }

        viewModel.message.observe(this) { texto ->
            pbLoading.visibility = View.GONE
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
        }
    }
}