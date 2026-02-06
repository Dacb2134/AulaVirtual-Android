package com.practicas.aulavirtualapp.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.DiscussionAdapter
import com.practicas.aulavirtualapp.model.AddDiscussionResponse
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
    private lateinit var swipeRefresh: SwipeRefreshLayout // Referencia al refresh
    private var token: String = ""
    private var forum: Forum? = null
    private var courseColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_detail)

        // 1. Recibir Datos
        token = intent.getStringExtra("USER_TOKEN") ?: ""
        forum = intent.getSerializableExtra("FORUM_DATA") as? Forum
        val defaultColor = ContextCompat.getColor(this, R.color.primary)
        courseColor = intent.getIntExtra("COURSE_COLOR", defaultColor)

        if (forum == null) {
            Toast.makeText(this, "Error cargando foro", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Referencias UI
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)
        val tvToolbarTitle = findViewById<TextView>(R.id.tvToolbarTitle)

        // Elementos del Documento
        val tvForumTitle = findViewById<TextView>(R.id.tvForumTitle)
        val layoutDueDate = findViewById<LinearLayout>(R.id.layoutDueDate)
        val tvDueDate = findViewById<TextView>(R.id.tvDueDate)
        val tvIntro = findViewById<TextView>(R.id.tvForumIntro)

        // Alertas Moodle
        val tvTypeAlert = findViewById<TextView>(R.id.tvForumTypeAlert)
        val cardCutoff = findViewById<MaterialCardView>(R.id.cardCutoffAlert)

        val rv = findViewById<RecyclerView>(R.id.rvDiscussions)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // Referencia al SwipeRefresh
        swipeRefresh = findViewById(R.id.swipeRefresh)

        // 3. Colores y Configuración
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        appBar.setBackgroundColor(courseColor)
        fab.backgroundTintList = ColorStateList.valueOf(courseColor)
        window.statusBarColor = courseColor
        swipeRefresh.setColorSchemeColors(courseColor) // Spinner con color del curso

        // 4. Llenado de Datos (Estilo Web Moodle)

        // A. Títulos
        tvToolbarTitle.text = "Foro"
        tvForumTitle.text = forum?.name

        // B. Fecha de Vencimiento
        if (forum!!.dueDate > 0) {
            layoutDueDate.visibility = View.VISIBLE
            val date = Date(forum!!.dueDate * 1000L)
            val format = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES"))
            tvDueDate.text = format.format(date)
        } else {
            layoutDueDate.visibility = View.GONE
        }

        // C. Descripción HTML
        if (forum!!.intro.isNotEmpty()) {
            tvIntro.text = HtmlCompat.fromHtml(forum!!.intro, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvIntro.movementMethod = LinkMovementMethod.getInstance()
            tvIntro.visibility = View.VISIBLE
        } else {
            tvIntro.visibility = View.GONE
        }

        // D. Alertas
        tvTypeAlert.text = getForumTypeMessage(forum?.type)
        val now = System.currentTimeMillis() / 1000
        cardCutoff.visibility = if (forum!!.cutoffDate > 0 && now > forum!!.cutoffDate) View.VISIBLE else View.GONE

        // 5. Configurar RecyclerView
        adapter = DiscussionAdapter { discussion ->
            Toast.makeText(this, "Abriendo hilo: ${discussion.name}", Toast.LENGTH_SHORT).show()
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // --- FUNCIONALIDAD AGREGADA ---

        // A. Swipe to Refresh
        swipeRefresh.setOnRefreshListener {
            loadDiscussions(progressBar, tvEmpty, isRefreshing = true)
        }

        // B. Carga Inicial
        loadDiscussions(progressBar, tvEmpty)

        // C. Botón Crear (Abre Overlay)
        fab.setOnClickListener {
            showCreateDiscussionDialog()
        }

        // D. Permisos
        checkPermissions(fab)
    }

    // --- LÓGICA DEL DIÁLOGO (OVERLAY) ---
    private fun showCreateDiscussionDialog() {
        val dialog = BottomSheetDialog(this)
        // Inflamos el layout del diálogo que creamos antes
        val view = layoutInflater.inflate(R.layout.dialog_create_discussion, null)
        dialog.setContentView(view)

        val etSubject = view.findViewById<TextInputEditText>(R.id.etSubject)
        val etMessage = view.findViewById<TextInputEditText>(R.id.etMessage)
        val btnPost = view.findViewById<MaterialButton>(R.id.btnPost)

        // Pintar el botón del color del curso para consistencia
        btnPost.backgroundTintList = ColorStateList.valueOf(courseColor)

        btnPost.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Por favor completa el asunto y el mensaje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bloquear botón para evitar doble envío
            btnPost.isEnabled = false
            btnPost.text = "Enviando..."

            // Llamada a la API
            RetrofitClient.instance.addDiscussion(token, forumId = forum!!.id, subject = subject, message = message)
                .enqueue(object : Callback<AddDiscussionResponse> {
                    override fun onResponse(call: Call<AddDiscussionResponse>, response: Response<AddDiscussionResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val resp = response.body()!!

                            // 1. Mostrar mensaje de éxito (Desde el JSON o genérico)
                            val successMsg = resp.messages?.firstOrNull { it.type == "success" }?.message
                                ?: "¡Debate publicado con éxito!"
                            Toast.makeText(this@ForumDetailActivity, successMsg, Toast.LENGTH_LONG).show()

                            // 2. Cerrar Overlay
                            dialog.dismiss()

                            // 3. Recargar lista automáticamente
                            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                            val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
                            loadDiscussions(progressBar, tvEmpty, isRefreshing = true)
                        } else {
                            btnPost.isEnabled = true
                            btnPost.text = "Publicar en el foro"
                            Toast.makeText(this@ForumDetailActivity, "Error al publicar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AddDiscussionResponse>, t: Throwable) {
                        btnPost.isEnabled = true
                        btnPost.text = "Publicar en el foro"
                        Toast.makeText(this@ForumDetailActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
        }
        dialog.show()
    }

    // Método actualizado para soportar SwipeRefresh
    private fun loadDiscussions(progressBar: ProgressBar, tvEmpty: TextView, isRefreshing: Boolean = false) {
        if (!isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }
        tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getForumDiscussions(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<ForumDiscussionResponse> {
                override fun onResponse(call: Call<ForumDiscussionResponse>, response: Response<ForumDiscussionResponse>) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false // Detener spinner

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
                    swipeRefresh.isRefreshing = false // Detener spinner
                    Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkPermissions(fab: FloatingActionButton) {
        fab.hide()
        RetrofitClient.instance.getForumAccess(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val perms = response.body()
                        val canPost = perms?.get("canstartdiscussion") as? Boolean ?: false
                        if (canPost) fab.show()
                    }
                }
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
            })
    }

    private fun getForumTypeMessage(type: String?): String {
        return when (type) {
            "qanda" -> "Este es un foro de Preguntas y Respuestas. Para ver otras respuestas, debe primero enviar la suya."
            "eachuser" -> "Cada persona plantea un tema y todos pueden responder."
            "single" -> "Este es un debate único y sencillo. Todos responden al mismo tema."
            "news" -> "Foro de Avisos y Novedades generales."
            "blog" -> "Foro estándar que se muestra en formato de blog."
            else -> "Foro de uso general para debates abiertos."
        }
    }
}