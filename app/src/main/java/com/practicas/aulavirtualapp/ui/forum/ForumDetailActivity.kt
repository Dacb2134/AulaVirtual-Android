package com.practicas.aulavirtualapp.ui.forum

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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.DiscussionAdapter
import com.practicas.aulavirtualapp.model.AddDiscussionResponse
import com.practicas.aulavirtualapp.model.Forum
import com.practicas.aulavirtualapp.model.ForumDiscussion
import com.practicas.aulavirtualapp.model.ForumDiscussionResponse
import com.practicas.aulavirtualapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ForumDetailActivity : AppCompatActivity() {

    private lateinit var adapter: DiscussionAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var token: String = ""
    private var forum: Forum? = null
    private var courseColor: Int = 0

    // Referencias UI globales
    private lateinit var tvToolbarTitle: TextView
    private lateinit var tvForumTitle: TextView
    private lateinit var layoutDueDate: LinearLayout
    private lateinit var tvDueDate: TextView
    private lateinit var tvIntro: TextView
    private lateinit var tvTypeAlert: TextView
    private lateinit var cardCutoff: MaterialCardView
    private lateinit var fab: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvSectionTitle: TextView

    // Referencias para el Modo Escritura (Nuevo Tema)
    private lateinit var layoutListContainer: LinearLayout
    private lateinit var layoutCreateForm: LinearLayout
    private lateinit var etSubject: TextInputEditText
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSubmit: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_detail)

        // 1. Recibir Datos
        token = intent.getStringExtra("USER_TOKEN") ?: ""
        forum = intent.getSerializableExtra("FORUM_DATA") as? Forum
        val defaultColor = ContextCompat.getColor(this, R.color.primary)
        courseColor = intent.getIntExtra("COURSE_COLOR", defaultColor)

        if (forum == null) {
            Toast.makeText(this, "Error cargando datos del foro", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Inicializar Referencias UI
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        tvForumTitle = findViewById(R.id.tvForumTitle)
        layoutDueDate = findViewById(R.id.layoutDueDate)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvIntro = findViewById(R.id.tvForumIntro)
        tvTypeAlert = findViewById(R.id.tvForumTypeAlert)
        cardCutoff = findViewById(R.id.cardCutoffAlert)
        fab = findViewById(R.id.fabAddTopic)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        tvSectionTitle = findViewById(R.id.tvSectionTitle)

        // Referencias del nuevo formulario
        layoutListContainer = findViewById(R.id.layoutListContainer)
        layoutCreateForm = findViewById(R.id.layoutCreateForm)
        etSubject = findViewById(R.id.etSubject)
        etMessage = findViewById(R.id.etMessage)
        btnCancel = findViewById(R.id.btnCancel)
        btnSubmit = findViewById(R.id.btnSubmit)

        val rv = findViewById<RecyclerView>(R.id.rvDiscussions)

        // 3. Configuración Visual
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            // UX: Si está escribiendo tema nuevo, cancelar primero
            if (layoutCreateForm.visibility == View.VISIBLE) {
                toggleWriteMode(false)
            } else {
                finish()
            }
        }

        appBar.setBackgroundColor(courseColor)
        fab.backgroundTintList = ColorStateList.valueOf(courseColor)
        window.statusBarColor = courseColor
        swipeRefresh.setColorSchemeColors(courseColor)
        btnSubmit.backgroundTintList = ColorStateList.valueOf(courseColor)

        // 4. Configurar RecyclerView con Lógica de Respuesta
        adapter = DiscussionAdapter { discussion, replyMessage ->
            if (replyMessage == null) {
                // Click normal -> Ver detalle del hilo (Navegación futura)
                Toast.makeText(this, "Abriendo hilo completo...", Toast.LENGTH_SHORT).show()
            } else {
                // Mensaje presente -> Es una RESPUESTA rápida
                postReply(discussion, replyMessage)
            }
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 5. Carga Inicial
        updateForumUI()
        loadDiscussions(isRefreshing = false)
        checkPermissionsAndDates()

        // 6. Listeners
        swipeRefresh.setOnRefreshListener {
            performFullRefresh()
        }

        // Botón Nuevo Tema
        fab.setOnClickListener {
            toggleWriteMode(true)
        }

        btnCancel.setOnClickListener {
            toggleWriteMode(false)
        }

        btnSubmit.setOnClickListener {
            postNewDiscussion() // Ojo: Nombre cambiado para distinguir de postReply
        }
    }

    // --- FUNCIÓN 1: ENVIAR RESPUESTA A UN POST (REPLY) ---
    private fun postReply(discussion: ForumDiscussion, message: String) {
        val subject = "Re: ${discussion.name}"
        Toast.makeText(this, "Enviando respuesta...", Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.addDiscussionPost(
            token = token,
            postId = discussion.id, // ID del hilo/mensaje padre
            subject = subject,
            message = message
        ).enqueue(object : Callback<AddDiscussionResponse> {
            override fun onResponse(call: Call<AddDiscussionResponse>, response: Response<AddDiscussionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!
                    // Moodle devuelve mensajes de éxito, podemos usarlos
                    val msg = resp.messages?.firstOrNull()?.message ?: "¡Respuesta enviada!"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

                    // Recargamos para ver el contador de respuestas aumentar
                    performFullRefresh()
                } else {
                    Toast.makeText(applicationContext, "Error al enviar respuesta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddDiscussionResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- FUNCIÓN 2: CREAR NUEVO TEMA (NEW DISCUSSION) ---
    private fun postNewDiscussion() {
        val subject = etSubject.text.toString().trim()
        val message = etMessage.text.toString().trim()

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Enviando..."

        RetrofitClient.instance.addDiscussion(
            token = token,
            forumId = forum!!.id,
            subject = subject,
            message = message
        ).enqueue(object : Callback<AddDiscussionResponse> {
            override fun onResponse(call: Call<AddDiscussionResponse>, response: Response<AddDiscussionResponse>) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Publicar"

                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!
                    val successMsg = resp.messages?.firstOrNull { it.type == "success" }?.message
                        ?: "Publicado con éxito"
                    Toast.makeText(this@ForumDetailActivity, successMsg, Toast.LENGTH_LONG).show()

                    toggleWriteMode(false)
                    performFullRefresh()
                } else {
                    Toast.makeText(this@ForumDetailActivity, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddDiscussionResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Publicar"
                Toast.makeText(this@ForumDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleWriteMode(enable: Boolean) {
        if (enable) {
            layoutListContainer.visibility = View.GONE
            layoutCreateForm.visibility = View.VISIBLE
            fab.hide()
            etSubject.text?.clear()
            etMessage.text?.clear()
            etSubject.requestFocus()
        } else {
            layoutListContainer.visibility = View.VISIBLE
            layoutCreateForm.visibility = View.GONE
            checkPermissionsAndDates()
        }
    }

    private fun updateForumUI() {
        if (forum == null) return

        tvToolbarTitle.text = "Foro"
        tvForumTitle.text = forum?.name

        if (forum!!.dueDate > 0) {
            layoutDueDate.visibility = View.VISIBLE
            val date = Date(forum!!.dueDate * 1000L)
            val format = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES"))
            tvDueDate.text = format.format(date)
        } else {
            layoutDueDate.visibility = View.GONE
        }

        if (forum!!.intro.isNotEmpty()) {
            tvIntro.text = HtmlCompat.fromHtml(forum!!.intro, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvIntro.movementMethod = LinkMovementMethod.getInstance()
            tvIntro.visibility = View.VISIBLE
        } else {
            tvIntro.visibility = View.GONE
        }

        tvTypeAlert.text = getForumTypeMessage(forum?.type)

        val now = System.currentTimeMillis() / 1000
        val isExpired = forum!!.cutoffDate > 0 && now > forum!!.cutoffDate
        cardCutoff.visibility = if (isExpired) View.VISIBLE else View.GONE
    }

    private fun performFullRefresh() {
        RetrofitClient.instance.getForumsByCourse(token = token, courseId = forum!!.courseId)
            .enqueue(object : Callback<List<Forum>> {
                override fun onResponse(call: Call<List<Forum>>, response: Response<List<Forum>>) {
                    if (response.isSuccessful) {
                        val updatedForum = response.body()?.find { it.id == forum!!.id }
                        if (updatedForum != null) {
                            forum = updatedForum
                            updateForumUI()
                            checkPermissionsAndDates()
                        }
                    }
                    loadDiscussions(isRefreshing = true)
                }

                override fun onFailure(call: Call<List<Forum>>, t: Throwable) {
                    loadDiscussions(isRefreshing = true)
                }
            })
    }

    private fun loadDiscussions(isRefreshing: Boolean = false) {
        if (!isRefreshing) progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getForumDiscussions(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<ForumDiscussionResponse> {
                override fun onResponse(call: Call<ForumDiscussionResponse>, response: Response<ForumDiscussionResponse>) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    if (response.isSuccessful) {
                        val discussions = response.body()?.discussions ?: emptyList()
                        if (discussions.isNotEmpty()) {
                            adapter.updateData(discussions)
                        } else {
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<ForumDiscussionResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkPermissionsAndDates() {
        val now = System.currentTimeMillis() / 1000
        val isExpired = forum!!.cutoffDate > 0 && now > forum!!.cutoffDate

        if (isExpired) {
            fab.hide()
            return
        }

        if (layoutCreateForm.visibility == View.VISIBLE) {
            fab.hide()
            return
        }

        fab.hide()
        RetrofitClient.instance.getForumAccess(token = token, forumId = forum!!.id)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val perms = response.body()
                        val canPost = perms?.get("canstartdiscussion") as? Boolean ?: false
                        if (canPost && !isExpired && layoutCreateForm.visibility != View.VISIBLE) {
                            fab.show()
                        }
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