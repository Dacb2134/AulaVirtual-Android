package com.practicas.aulavirtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.practicas.aulavirtualapp.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Botón atrás
        supportActionBar?.setDisplayShowTitleEnabled(false) // Quitamos título default

        // Manejar flecha atrás
        toolbar.setNavigationOnClickListener { finish() }

        // Referencias a los botones
        val btnSecurity = findViewById<LinearLayout>(R.id.btnSecurity)
        val btnNotifications = findViewById<LinearLayout>(R.id.btnNotifications)
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        val btnAbout = findViewById<LinearLayout>(R.id.btnAbout)

        // ACCIONES
        btnSecurity.setOnClickListener {
            Toast.makeText(this, "Abrir Web de Moodle para cambiar clave", Toast.LENGTH_SHORT).show()

        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Configuración de Notificaciones (Próximamente)", Toast.LENGTH_SHORT).show()
        }

        btnAbout.setOnClickListener {
            Toast.makeText(this, "AulaVirtual App v1.0\nDesarrollado con pasión.", Toast.LENGTH_LONG).show()
        }

        btnLogout.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}