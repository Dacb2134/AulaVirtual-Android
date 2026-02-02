package com.practicas.aulavirtualapp

import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class CourseDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // Recibimos los datos de la pantalla anterior
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Curso"
        val color = intent.getIntExtra("COURSE_COLOR", 0)

        // Pintamos la pantalla
        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        val header = findViewById<View>(R.id.viewHeader)

        tvTitle.text = courseName
        if (color != 0) {
            header.setBackgroundColor(color)
        }
    }
}