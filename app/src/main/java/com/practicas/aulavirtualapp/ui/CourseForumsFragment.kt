package com.practicas.aulavirtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout // 👈 Importante
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.ForumAdapter
import com.practicas.aulavirtualapp.model.Forum
import com.practicas.aulavirtualapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseForumsFragment : Fragment(R.layout.fragment_course_forums) {

    private lateinit var adapter: ForumAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout // 👈 Variable para el Swipe
    private var courseId: Int = 0
    private var token: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recuperar argumentos
        var courseColor = 0
        arguments?.let {
            courseId = it.getInt("COURSE_ID")
            token = it.getString("USER_TOKEN") ?: ""
            courseColor = it.getInt("COURSE_COLOR")
        }

        // 2. Configurar Vistas
        val rvForums = view.findViewById<RecyclerView>(R.id.rvForums)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        swipeRefresh = view.findViewById(R.id.swipeRefresh) // 👈 Buscamos la vista en el XML

        // Configurar color del spinner de carga (Para que combine con el curso)
        if (courseColor != 0) {
            swipeRefresh.setColorSchemeColors(courseColor)
        }

        adapter = ForumAdapter { forum ->
            val intent = Intent(requireContext(), ForumDetailActivity::class.java)
            // Pasamos el objeto completo para tener fechas y descripción
            intent.putExtra("FORUM_DATA", forum)
            intent.putExtra("USER_TOKEN", token)
            intent.putExtra("COURSE_COLOR", courseColor)
            startActivity(intent)
        }

        rvForums.layoutManager = LinearLayoutManager(requireContext())
        rvForums.adapter = adapter

        // 3. Listener del Swipe Refresh (Al deslizar)
        swipeRefresh.setOnRefreshListener {
            loadForums(progressBar, tvEmpty, isRefreshing = true)
        }

        // 4. Carga datos inicial
        loadForums(progressBar, tvEmpty)
    }

    // Modificamos la función para aceptar el estado "isRefreshing"
    private fun loadForums(progressBar: ProgressBar, tvEmpty: TextView, isRefreshing: Boolean = false) {
        if (token.isEmpty() || courseId == 0) {
            swipeRefresh.isRefreshing = false
            return
        }

        // Si NO estamos refrescando con el dedo, mostramos la barra de carga central normal
        if (!isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }
        tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getForumsByCourse(token = token, courseId = courseId)
            .enqueue(object : Callback<List<Forum>> {
                override fun onResponse(call: Call<List<Forum>>, response: Response<List<Forum>>) {
                    // Detenemos todas las animaciones de carga
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false // 👈 Detiene el spinner de arriba

                    if (response.isSuccessful) {
                        val forums = response.body() ?: emptyList()
                        if (forums.isNotEmpty()) {
                            adapter.updateData(forums)
                        } else {
                            tvEmpty.visibility = View.VISIBLE
                            // Limpiamos el adaptador por si antes había datos y ahora no
                            adapter.updateData(emptyList())
                        }
                    } else {
                        Toast.makeText(context, "Error al cargar foros", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Forum>>, t: Throwable) {
                    // Detenemos animaciones si falla
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(context, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}