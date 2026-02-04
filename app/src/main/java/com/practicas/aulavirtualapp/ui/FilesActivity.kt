package com.practicas.aulavirtualapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.network.PrivateFilesInfo
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilesActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvSummary: TextView
    private lateinit var tvDetail: TextView
    private lateinit var btnWeb: Button


    private val MOODLE_FILES_URL = "http://192.168.1.144/user/files.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)


        authRepository = AuthRepository()

        // Referencias UI
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFiles)
        pbLoading = findViewById(R.id.pbLoadingFiles)
        tvSummary = findViewById(R.id.tvFileSummary)
        tvDetail = findViewById(R.id.tvFileDetail)
        btnWeb = findViewById(R.id.btnOpenWeb)

        // Configurar Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Cargar datos
        loadFilesInfo()

        // Configurar Botón Web
        btnWeb.setOnClickListener {
            openMoodleWeb()
        }
    }

    private fun loadFilesInfo() {
        // PRIMERO: Intentamos obtener los datos que nos pasó ProfileFragment
        var token = intent.getStringExtra("USER_TOKEN")
        var userId = intent.getIntExtra("USER_ID", -1)

        // 2SEGUNDO: Si llegaron vacíos (por si abres la activity desde otro lado),

        if (token.isNullOrEmpty() || userId == -1) {
            val sharedPreferences = getSharedPreferences("aula_virtual_prefs", Context.MODE_PRIVATE)
            token = sharedPreferences.getString("token", "")
            userId = sharedPreferences.getInt("user_id", -1)
        }

        // Verificamos si conseguimos los datos (de una forma u otra)
        if (!token.isNullOrEmpty() && userId != -1) {
            pbLoading.visibility = View.VISIBLE

            // Llamada a la API
            authRepository.getFilesInfo(token, userId).enqueue(object : Callback<PrivateFilesInfo> {
                override fun onResponse(call: Call<PrivateFilesInfo>, response: Response<PrivateFilesInfo>) {
                    pbLoading.visibility = View.GONE
                    if (response.isSuccessful) {
                        updateUI(response.body())
                    } else {
                        tvSummary.text = "Error de sincronización"
                        tvDetail.text = "Código: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<PrivateFilesInfo>, t: Throwable) {
                    pbLoading.visibility = View.GONE
                    tvSummary.text = "Sin conexión"
                    Toast.makeText(this@FilesActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(this, "Sesión no válida, reinicia la app", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun updateUI(info: PrivateFilesInfo?) {
        if (info != null) {
            val count = info.filecount
            // Convertir Bytes a MB
            val sizeMb = info.filesize.toDouble() / (1024 * 1024)
            val sizeFormatted = String.format("%.2f MB", sizeMb)

            if (count > 0) {
                tvSummary.text = "Tienes $count archivo(s)"
                tvDetail.text = "Ocupan $sizeFormatted en tu nube."
            } else {
                tvSummary.text = "Carpeta vacía"
                tvDetail.text = "No tienes archivos privados."
            }
        } else {
            tvSummary.text = "Información no disponible"
        }
    }

    private fun openMoodleWeb() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(MOODLE_FILES_URL)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró navegador", Toast.LENGTH_SHORT).show()
        }
    }
}