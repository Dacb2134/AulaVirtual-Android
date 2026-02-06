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
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.ForumAdapter
import com.practicas.aulavirtualapp.model.Forum
import com.practicas.aulavirtualapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseForumsFragment : Fragment(R.layout.fragment_course_forums) {

    private lateinit var adapter: ForumAdapter
    private var courseId: Int = 0
    private var token: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recuperar argumentos (pasados desde CourseDetailActivity)
        arguments?.let {
            courseId = it.getInt("COURSE_ID")
            token = it.getString("USER_TOKEN") ?: ""
        }

        // 2. Configurar RecyclerView
        val rvForums = view.findViewById<RecyclerView>(R.id.rvForums)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = ForumAdapter { forum ->
            // AQUÍ ABRIREMOS LA ACTIVIDAD DE DETALLE DEL FORO (Paso siguiente)
            // val intent = Intent(requireContext(), ForumDetailActivity::class.java)
            // intent.putExtra("FORUM_ID", forum.id)
            // intent.putExtra("FORUM_NAME", forum.name)
            // intent.putExtra("USER_TOKEN", token)
            // startActivity(intent)
            Toast.makeText(requireContext(), "Abrir foro: ${forum.name}", Toast.LENGTH_SHORT).show()
        }

        rvForums.layoutManager = LinearLayoutManager(requireContext())
        rvForums.adapter = adapter

        // 3. Cargar datos desde la API
        loadForums(progressBar, tvEmpty, rvForums)
    }

    private fun loadForums(progressBar: ProgressBar, tvEmpty: TextView, rv: RecyclerView) {
        if (token.isEmpty() || courseId == 0) return

        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        // Llamada a tu API definida en MoodleApiService
        RetrofitClient.instance.getForumsByCourse(token = token, courseId = courseId)
            .enqueue(object : Callback<List<Forum>> {
                override fun onResponse(call: Call<List<Forum>>, response: Response<List<Forum>>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val forums = response.body() ?: emptyList()
                        if (forums.isNotEmpty()) {
                            adapter.updateData(forums)
                        } else {
                            tvEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(context, "Error al cargar foros", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Forum>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}