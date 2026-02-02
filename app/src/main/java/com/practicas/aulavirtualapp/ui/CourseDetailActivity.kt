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

        // 1. Recibir datos (incluyendo el TOKEN)
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Curso"
        val courseId = intent.getIntExtra("COURSE_ID", 0)
        val courseColor = intent.getIntExtra("COURSE_COLOR", 0)
        val token = intent.getStringExtra("USER_TOKEN") // <--- Importante

        // 2. Configurar diseño visual
        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        val header = findViewById<View>(R.id.viewHeader)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val rvAssignments = findViewById<RecyclerView>(R.id.rvAssignments)

        tvTitle.text = courseName
        if (courseColor != 0) header.setBackgroundColor(courseColor)

        // 3. Configurar la Lista (RecyclerView)
        rvAssignments.layoutManager = LinearLayoutManager(this)
        adapter = AssignmentAdapter()
        rvAssignments.adapter = adapter

        // 4. Conectar con el ViewModel
        viewModel = ViewModelProvider(this)[CourseDetailViewModel::class.java]

        // Si tenemos token y ID, pedimos las tareas
        if (token != null && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadAssignments(token, courseId)
        } else {
            Toast.makeText(this, "Error: Datos del curso incompletos", Toast.LENGTH_SHORT).show()
        }

        // 5. Observar respuestas
        viewModel.assignments.observe(this) { listaTareas ->
            pbLoading.visibility = View.GONE
            adapter.updateData(listaTareas)
        }

        viewModel.message.observe(this) { texto ->
            pbLoading.visibility = View.GONE
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
        }
    }
}