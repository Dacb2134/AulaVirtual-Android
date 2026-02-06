package com.practicas.aulavirtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.practicas.aulavirtualapp.ui.HomeActivity
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.UserRole
import com.practicas.aulavirtualapp.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    // Lanzador de Google Login
    private val googleLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)

            // 👇 NUEVO: Pedimos el email directo
            val email = account?.email
            val name = account?.displayName ?: "Usuario Google"

            if (!email.isNullOrBlank()) {
                Toast.makeText(this, "Conectando... $email", Toast.LENGTH_SHORT).show()
                // Llamamos a la nueva función de lógica
                viewModel.realizarLoginGoogle(email, name)
            } else {
                Toast.makeText(this, "Google no devolvió el email.", Toast.LENGTH_SHORT).show()
            }
        } catch (exception: ApiException) {
            Toast.makeText(this, "Error Google: ${exception.statusCode}. Revisa 'Test Users' en Cloud Console.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // OBSERVAMOS los cambios
        viewModel.cargando.observe(this) { estaCargando ->
            if (estaCargando) {
                tvStatus.text = "Conectando con Moodle..."
                btnLogin.isEnabled = false
                btnGoogleLogin.isEnabled = false
            } else {
                btnLogin.isEnabled = true
                btnGoogleLogin.isEnabled = true
            }
        }

        viewModel.resultadoLogin.observe(this) { response ->
            if (response != null) {
                tvStatus.text = "¡Login Correcto!"
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()

                val destination = when (response.role) {
                    UserRole.ADMIN -> AdminHomeActivity::class.java
                    UserRole.DOCENTE -> TeacherHomeActivity::class.java
                    UserRole.ESTUDIANTE -> HomeActivity::class.java
                }
                val intent = Intent(this, destination)
                intent.putExtra("USER_TOKEN", response.token)
                intent.putExtra("USER_ID", response.userId)

                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { mensajeError ->
            tvStatus.text = "Error: $mensajeError"
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }

        // Login Manual
        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.realizarLogin(user, pass)
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configuración de Google
        val googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleOptions)

        btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleLoginLauncher.launch(signInIntent)
            }
        }
    }
}