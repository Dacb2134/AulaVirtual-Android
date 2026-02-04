package com.practicas.aulavirtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout // IMPORTADO
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.CourseAdapter
import com.practicas.aulavirtualapp.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoading)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        // NUEVO: Referencia Swipe
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshHome)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)

        rvCourses.layoutManager = LinearLayoutManager(context)

        adapter = CourseAdapter(emptyList()) { course, color ->
            val nextIntent = Intent(context, CourseDetailActivity::class.java)
            nextIntent.putExtra("COURSE_ID", course.id)
            nextIntent.putExtra("COURSE_NAME", course.fullName)
            nextIntent.putExtra("COURSE_COLOR", color)

            val myToken = requireActivity().intent.getStringExtra("USER_TOKEN")
            nextIntent.putExtra("USER_TOKEN", myToken)

            startActivity(nextIntent)
        }
        rvCourses.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val token = requireActivity().intent.getStringExtra("USER_TOKEN")

        // NUEVO: Listener del Swipe
        swipeRefresh.setOnRefreshListener {
            if (token != null) {
                viewModel.cargarDatosUsuario(token)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }

        if (token != null) {
            // Solo mostramos el zorro central si NO estamos refrescando con el dedo
            if (!swipeRefresh.isRefreshing) pbLoading.visibility = View.VISIBLE
            viewModel.cargarDatosUsuario(token)
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { texto ->
            if (texto.contains("Hola")) {
                tvGreeting?.text = texto.split(".")[0] + " 👋"
            } else if (texto.contains("Error") || texto.contains("Fallo")) {
                pbLoading.visibility = View.GONE
                swipeRefresh.isRefreshing = false // Detener swipe
                Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.cursos.observe(viewLifecycleOwner) { listaCursos ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false // Detener swipe

            if (listaCursos.isNotEmpty()) {
                adapter.updateData(listaCursos)
            } else {
                tvGreeting?.text = "Sin cursos asignados"
            }
        }
    }
}