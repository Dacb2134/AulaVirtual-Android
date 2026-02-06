package com.practicas.aulavirtualapp.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.DiscussionAdapter
import com.practicas.aulavirtualapp.model.Forum
import com.practicas.aulavirtualapp.model.ForumDiscussionResponse
import com.practicas.aulavirtualapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ForumDetailActivity : AppCompatActivity() {

    private lateinit var adapter: DiscussionAdapter
    private var token: String = ""
    private var forum: Forum? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_detail)

        // 1. Recibir Datos
        token = intent.getStringExtra("USER_TOKEN") ?: ""
        forum = intent.getSerializableExtra("FORUM_DATA") as? Forum
        val defaultColor = ContextCompat.getColor(this, R.color.primary)
        val courseColor = intent.getIntExtra("COURSE_COLOR", defaultColor)

        if (forum == null) {
            Toast.makeText(this, "Error cargando foro", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Referencias UI (Adaptadas al nuevo diseño Moodle)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)
        val tvToolbarTitle = findViewById<TextView>(R.id.tvToolbarTitle) // Título pequeño en barra

        // Elementos del Documento
        val tvForumTitle = findViewById<TextView>(R.id.tvForumTitle) // Título Grande
        val layoutDueDate = findViewById<LinearLayout>(R.id.layoutDueDate)
        val tvDueDate = findViewById<TextView>(R.id.tvDueDate)
        val tvIntro = findViewById<TextView>(R.id.tvForumIntro)

        // Alertas Moodle (Cajas de aviso)
        val tvTypeAlert = findViewById<TextView>(R.id.tvForumTypeAlert)
        val cardCutoff = findViewById<MaterialCardView>(R.id.cardCutoffAlert)

        val rv = findViewById<RecyclerView>(R.id.rvDiscussions)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // 3. Colores y Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        appBar.setBackgroundColor(courseColor)
        fab.backgroundTintList = ColorStateList.valueOf(courseColor)
        window.statusBarColor = courseColor

        // 4. Llenado de Datos (Estilo Web Moodle)

        // A. Títulos
        tvToolbarTitle.text = "Foro"
        tvForumTitle.text = forum?.name

        // B. Fecha de Vencimiento
        if (forum!!.dueDate > 0) {
            layoutDueDate.visibility = View.VISIBLE
            // Formato largo en español: "miércoles, 4 de febrero..."
            val date = Date(forum!!.dueDate * 1000L)
            val format = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES"))
            tvDueDate.text = format.format(date)
        } else {
            layoutDueDate.visibility = View.GONE
        }

        // C. Descripción HTML (Con links funcionales)
        if (forum!!.intro.isNotEmpty()) {
            tvIntro.text = HtmlCompat.fromHtml(forum!!.intro, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvIntro.movementMethod = LinkMovementMethod.getInstance() // Permite clicks en enlaces
            tvIntro.visibility = View.VISIBLE
        } else {
            tvIntro.visibility = View.GONE
        }

        // D. Alerta de Tipo de Foro (La caja azul explicativa)
        tvTypeAlert.text = getForumTypeMessage(forum?.type)

        // E. Alerta de Cierre (Caja roja si ya pasó la fecha)
        val now = System.currentTimeMillis() / 1000
        if (forum!!.cutoffDate > 0 && now > forum!!.cutoffDate) {
            cardCutoff.visibility = View.VISIBLE
        } else {
            cardCutoff.visibility = View.GONE
        }

        // 5. Configurar RecyclerView
        adapter = DiscussionAdapter { discussion ->
            Toast.makeText(this, "Abriendo hilo...", Toast.LENGTH_SHORT).show()
            // Aquí conectarás con la pantalla de lectura de mensajes más adelante
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 6. Cargar Datos y Permisos
        loadDiscussions(progressBar, tvEmpty)
        checkPermissions(fab)
    }

    // Traduce el tipo de foro al mensaje largo de Moodle
    // Traduce el código técnico de Moodle al mensaje real que ve el usuario
    private fun getForumTypeMessage(type: String?): String {
        return when (type) {
            // 👇 ESTE ES EL QUE BUSCAS
            "qanda" -> "Este es un foro de Preguntas y Respuestas. Para ver otras respuestas, debe primero enviar la suya."

            "eachuser" -> "Cada persona plantea un tema y todos pueden responder."

            "single" -> "Este es un debate único y sencillo. Todos responden al mismo tema."

            "news" -> "Foro de Avisos y Novedades generales."

            "blog" -> "Foro estándar que se muestra en formato de blog."


            else -> "Foro de uso general para debates abiertos."
        }
    }

    private fun loadDiscussions(progressBar: ProgressBar, tvEmpty: TextView) {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getForumDiscussions(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<ForumDiscussionResponse> {
                override fun onResponse(call: Call<ForumDiscussionResponse>, response: Response<ForumDiscussionResponse>) {
                    progressBar.visibility = View.GONE
                    if(response.isSuccessful) {
                        val discussions = response.body()?.discussions ?: emptyList()
                        if(discussions.isNotEmpty()) {
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
        // Por defecto oculto
        fab.hide()

        RetrofitClient.instance.getForumAccess(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if(response.isSuccessful) {
                        val perms = response.body()
                        val canPost = perms?.get("canstartdiscussion") as? Boolean ?: false

                        // Si tiene permiso, mostramos el botón +
                        if(canPost) {
                            fab.show()
                        }
                    }
                }
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    // Si falla, se queda oculto por seguridad
                }
            })
    }
}