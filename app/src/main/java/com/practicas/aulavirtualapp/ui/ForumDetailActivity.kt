package com.practicas.aulavirtualapp.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.DiscussionAdapter
import com.practicas.aulavirtualapp.model.ForumDiscussionResponse
import com.practicas.aulavirtualapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForumDetailActivity : AppCompatActivity() {

    private lateinit var adapter: DiscussionAdapter
    private var token: String = ""
    private var forumId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_detail)

        // 1. Recibir datos del Intent
        token = intent.getStringExtra("USER_TOKEN") ?: ""
        forumId = intent.getIntExtra("FORUM_ID", 0)
        val forumName = intent.getStringExtra("FORUM_NAME") ?: "Foro"

        // Recibimos el color (Si no viene, usamos el color primario por defecto)
        val defaultColor = ContextCompat.getColor(this, R.color.primary)
        val courseColor = intent.getIntExtra("COURSE_COLOR", defaultColor)

        // 2. Referencias a la UI
        val tvTitle = findViewById<TextView>(R.id.tvHeaderTitle)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rv = findViewById<RecyclerView>(R.id.rvDiscussions)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        val header = findViewById<View>(R.id.viewHeader)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty) // Asegúrate de tener este ID en tu XML

        // APLICAR COLORES PARA CONTINUIDAD VISUAL ---
        header.setBackgroundColor(courseColor)
        fab.backgroundTintList = ColorStateList.valueOf(courseColor)

        // TOQUE PRO: Cambiar también la barra de estado (donde está la hora y batería)
        window.statusBarColor = courseColor

        // 4. Configurar Textos y Botones
        tvTitle.text = forumName
        btnBack.setOnClickListener { finish() }

        // 5. Configurar RecyclerView
        adapter = DiscussionAdapter { discussion ->
            // AQUÍ VEREMOS EL HILO (Próximo paso: Crear Activity de Lectura)
            Toast.makeText(this, "Leyendo: ${discussion.name}", Toast.LENGTH_SHORT).show()
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 6. Cargar Datos
        loadDiscussions(progressBar, tvEmpty)

        // 7. Verificar Permisos (Mostrar u ocultar botón +)
        checkPermissions(fab)
    }

    private fun loadDiscussions(progressBar: ProgressBar, tvEmpty: TextView) {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getForumDiscussions(token = token, forumId = forumId)
            .enqueue(object : Callback<ForumDiscussionResponse> {
                override fun onResponse(call: Call<ForumDiscussionResponse>, response: Response<ForumDiscussionResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val discussions = response.body()?.discussions ?: emptyList()

                        if (discussions.isNotEmpty()) {
                            adapter.updateData(discussions)
                        } else {
                            tvEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error al cargar debates", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ForumDiscussionResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkPermissions(fab: FloatingActionButton) {
        // Por defecto lo ocultamos para que no parpadee si no tiene permiso
        fab.hide()

        RetrofitClient.instance.getForumAccess(token = token, forumId = forumId)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val perms = response.body()
                        // Verificamos si tiene permiso de "startdiscussion"
                        val canPost = perms?.get("canstartdiscussion") as? Boolean ?: false

                        if (canPost) {
                            fab.show()
                        }
                    }
                }
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    // Si falla la red, asumimos que no tiene permiso por seguridad
                }
            })
    }
}