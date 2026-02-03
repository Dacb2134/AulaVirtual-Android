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
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.CourseAdapter
import com.practicas.aulavirtualapp.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: CourseAdapter

    // 1. Inflamos el diseño (Aquí pondremos el layout que incluye el Header y la Lista)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cargamos el diseño visual de este fragmento
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. RECUPERAMOS LAS VISTAS (Igual que en tu código original, pero usando 'view.')
        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoading)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        rvCourses.layoutManager = LinearLayoutManager(context)

        // 3. ADAPTADOR (Tu lógica exacta salvada aquí)
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

        // 4. VIEWMODEL (Tu lógica exacta salvada aquí)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val token = requireActivity().intent.getStringExtra("USER_TOKEN")
        if (token != null) {
            pbLoading.visibility = View.VISIBLE
            viewModel.cargarDatosUsuario(token)
        }

        //  OBSERVAR CAMBIOS
        viewModel.mensaje.observe(viewLifecycleOwner) { texto ->
            if (texto.contains("Hola")) {
                tvGreeting?.text = texto.split(".")[0] + " 👋"
            } else if (texto.contains("Error") || texto.contains("Fallo")) {
                pbLoading.visibility = View.GONE
                Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.cursos.observe(viewLifecycleOwner) { listaCursos ->
            pbLoading.visibility = View.GONE
            if (listaCursos.isNotEmpty()) {
                adapter.updateData(listaCursos)
            } else {
                tvGreeting?.text = "Sin cursos asignados"
            }
        }
    }
}