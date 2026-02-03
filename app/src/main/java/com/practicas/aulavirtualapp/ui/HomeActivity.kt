package com.practicas.aulavirtualapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicas.aulavirtualapp.ui.AgendaFragment
import com.practicas.aulavirtualapp.ui.GradesFragment
import com.practicas.aulavirtualapp.ui.HomeFragment
import com.practicas.aulavirtualapp.ui.ProfileFragment
import com.practicas.aulavirtualapp.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // mostramos HomeFragme
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment())
        }

        // Configurar los botones del menú
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cambiarFragmento(HomeFragment())
                R.id.nav_agenda -> cambiarFragmento(AgendaFragment())
                R.id.nav_grades -> cambiarFragmento(GradesFragment())
                R.id.nav_profile -> cambiarFragmento(ProfileFragment())
            }
            true
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}