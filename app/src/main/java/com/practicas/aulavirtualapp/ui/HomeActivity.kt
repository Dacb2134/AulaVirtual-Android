package com.practicas.aulavirtualapp.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicas.aulavirtualapp.ui.AgendaFragment
import com.practicas.aulavirtualapp.ui.GradesFragment
import com.practicas.aulavirtualapp.ui.HomeFragment
import com.practicas.aulavirtualapp.ui.ProfileFragment
import com.practicas.aulavirtualapp.R

open class HomeActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Permiso denegado. Puedes habilitarlo más tarde desde ajustes.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        requestStoragePermissionIfNeeded()

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

    private fun requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        }
    }
}
