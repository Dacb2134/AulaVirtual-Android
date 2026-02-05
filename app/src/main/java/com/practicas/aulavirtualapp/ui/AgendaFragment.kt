package com.practicas.aulavirtualapp.ui

import android.graphics.Color
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
import com.google.android.material.chip.ChipGroup
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.AgendaViewModel

class AgendaFragment : Fragment() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var viewModel: AgendaViewModel
    private var isUserRefreshing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAgenda = view.findViewById<RecyclerView>(R.id.rvAgenda)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoadingAgenda)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyAgenda)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilters)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshAgenda)
        swipeRefresh.setupBrandColors()

        // 🔑 Obtenemos los datos de sesión desde la Activity principal
        val token = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
        val userId = requireActivity().intent.getIntExtra("USER_ID", 0)

        rvAgenda.layoutManager = LinearLayoutManager(context)

        // 🔥 ADAPTER CORREGIDO: Ahora redirige directamente al detalle de la tarea (Deber)
        adapter = AssignmentAdapter(showCourseName = true) { assignment ->

            // 🚀 Usamos el método estático createIntent que definimos en AssignmentDetailActivity
            // Esto pasa automáticamente el token, colores y configuraciones de entrega.
            val intent = AssignmentDetailActivity.createIntent(
                context = requireContext(),
                assignment = assignment,
                fallbackCourseName = assignment.courseName,
                fallbackCourseColor = try {
                    Color.parseColor(assignment.courseColor)
                } catch (e: Exception) {
                    0
                },
                userToken = token
            )

            startActivity(intent)
        }

        rvAgenda.adapter = adapter

        viewModel = ViewModelProvider(this)[AgendaViewModel::class.java]

        // --- LÓGICA DE REFRESH ---
        swipeRefresh.setOnRefreshListener {
            if (token.isNotEmpty()) {
                isUserRefreshing = true
                viewModel.cargarAgendaGlobal(token, userId)
            } else {
                swipeRefresh.isRefreshing = false
                isUserRefreshing = false
            }
        }

        // --- FILTROS DE CHIPS ---
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> viewModel.filtrarTodas()
                    R.id.chipNext7Days -> viewModel.filtrarProximos7Dias()
                    R.id.chipOverdue -> viewModel.filtrarAtrasadas()
                }
            }
        }

        // --- OBSERVADORES DEL VIEWMODEL ---
        viewModel.cargando.observe(viewLifecycleOwner) { estaCargando ->
            swipeRefresh.isRefreshing = isUserRefreshing && estaCargando

            if (estaCargando && !isUserRefreshing) {
                pbLoading.visibility = View.VISIBLE
                rvAgenda.visibility = View.GONE
                tvEmpty.visibility = View.GONE
            } else {
                pbLoading.visibility = View.GONE
                if (!estaCargando) rvAgenda.visibility = View.VISIBLE
            }

            if (!estaCargando) {
                isUserRefreshing = false
            }
        }

        viewModel.agenda.observe(viewLifecycleOwner) { tareas ->
            if (tareas.isNotEmpty()) {
                adapter.updateData(tareas)
                tvEmpty.visibility = View.GONE
            } else {
                tvEmpty.text = "No se encontraron tareas con este filtro."
                tvEmpty.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            }
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { texto ->
            if (texto.contains("Error") || texto.contains("Fallo")) {
                Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
            } else if (texto.contains("No tienes cursos") || texto.contains("Todo al día")) {
                tvEmpty.text = texto
                tvEmpty.visibility = View.VISIBLE
            }
        }

        // Carga inicial
        if (token.isNotEmpty()) {
            viewModel.cargarAgendaGlobal(token, userId)
        } else {
            tvEmpty.text = "Error: Sesión no válida."
            tvEmpty.visibility = View.VISIBLE
        }
    }
}