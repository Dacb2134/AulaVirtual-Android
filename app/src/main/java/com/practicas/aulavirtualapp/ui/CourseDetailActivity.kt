package com.practicas.aulavirtualapp.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicas.aulavirtualapp.R

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var courseArgs: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // 1. Recibir datos del Intent
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Curso"
        val courseShortName = intent.getStringExtra("COURSE_SHORT_NAME") ?: ""
        val courseId = intent.getIntExtra("COURSE_ID", 0)
        val courseColor = intent.getIntExtra("COURSE_COLOR", 0)
        val token = intent.getStringExtra("USER_TOKEN")
        val userId = intent.getIntExtra("USER_ID", 0)

        if (token.isNullOrBlank() || courseId == 0) {
            Toast.makeText(this, "Error: Datos del curso incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Empaquetamos los datos para pasarlos a los fragmentos
        courseArgs = bundleOf(
            "COURSE_ID" to courseId,
            "COURSE_NAME" to courseName,
            "COURSE_SHORT_NAME" to courseShortName,
            "COURSE_COLOR" to courseColor,
            "USER_TOKEN" to token,
            "USER_ID" to userId
        )

        val header = findViewById<android.view.View>(R.id.viewHeader)
        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        val tvSubtitle = findViewById<TextView>(R.id.tvCourseSubtitle)
        val btnBack = findViewById<ImageButton>(R.id.btnCourseBack)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationCourse)

        tvTitle.text = courseName
        tvSubtitle.text = if (courseShortName.isNotBlank()) courseShortName else "Curso activo"
        if (courseColor != 0) header.setBackgroundColor(courseColor)

        btnBack.setOnClickListener { finish() }

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            cambiarFragmento(CourseOverviewFragment())
            bottomNav.selectedItemId = R.id.nav_course_overview
        }

        // Configurar navegación inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_course_overview -> cambiarFragmento(CourseOverviewFragment())
                R.id.nav_course_content -> cambiarFragmento(CourseContentFragment())
                R.id.nav_course_assignments -> cambiarFragmento(CourseAssignmentsFragment())
                R.id.nav_course_forum -> cambiarFragmento(CourseForumsFragment())
                R.id.nav_course_grades -> cambiarFragmento(CourseGradesFragment())
            }
            true
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        // Le pasamos siempre los argumentos (Token, ID curso, Color)
        fragment.arguments = Bundle(courseArgs)

        supportFragmentManager.beginTransaction()
            .replace(R.id.course_nav_host, fragment)
            .commit()
    }
}