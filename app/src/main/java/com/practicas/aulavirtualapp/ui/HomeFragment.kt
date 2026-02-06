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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.CourseAdapter
import com.practicas.aulavirtualapp.utils.setupBrandColors
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
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshHome)

        swipeRefresh.setupBrandColors()

        rvCourses.layoutManager = LinearLayoutManager(context)

        adapter = CourseAdapter(emptyList()) { course, color ->
            val nextIntent = Intent(context, CourseDetailActivity::class.java)
            nextIntent.putExtra("COURSE_ID", course.id)
            nextIntent.putExtra("COURSE_NAME", course.fullName)
            nextIntent.putExtra("COURSE_SHORT_NAME", course.shortName)
            nextIntent.putExtra("COURSE_COLOR", color)

            val myToken = requireActivity().intent.getStringExtra("USER_TOKEN")
            val myUserId = requireActivity().intent.getIntExtra("USER_ID", 0)
            nextIntent.putExtra("USER_TOKEN", myToken)
            nextIntent.putExtra("USER_ID", myUserId)

            startActivity(nextIntent)
        }
        rvCourses.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val token = requireActivity().intent.getStringExtra("USER_TOKEN")
        val userId = requireActivity().intent.getIntExtra("USER_ID", 0)

        swipeRefresh.setOnRefreshListener {
            if (token != null && userId != 0) {
                viewModel.cargarDatosUsuario(token, userId)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }

        if (token != null && userId != 0) {
            if (!swipeRefresh.isRefreshing) pbLoading.visibility = View.VISIBLE
            viewModel.cargarDatosUsuario(token, userId)
        } else {
            Toast.makeText(context, "Error cargando datos de usuario", Toast.LENGTH_SHORT).show()
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { texto ->
            if (texto.contains("Hola")) {
                tvGreeting?.text = texto.split(".")[0] + " 👋"
            } else if (texto.contains("Error") || texto.contains("Fallo")) {
                pbLoading.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.cursos.observe(viewLifecycleOwner) { listaCursos ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false

            if (listaCursos.isNotEmpty()) {
                adapter.updateData(listaCursos)
            } else {
                adapter.updateData(emptyList())
                tvGreeting?.text = "Sin cursos asignados"
            }
        }
    }
}