package com.practicas.aulavirtualapp.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.ChipGroup
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter
import com.practicas.aulavirtualapp.utils.AssignmentProgressStore
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.AgendaViewModel

class AgendaFragment : Fragment() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var viewModel: AgendaViewModel
    private var isUserRefreshing = false

    // 🚀 LAUNCHER: Escucha cuando regresas de AssignmentDetailActivity
    private val startDetailForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Si la tarea se marcó como hecha o se entregó, refrescamos la lista
            ejecutarCarga()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_agenda, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAgenda = view.findViewById<RecyclerView>(R.id.rvAgenda)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoadingAgenda)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyAgenda)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilters)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshAgenda)

        swipeRefresh.setupBrandColors()
        rvAgenda.layoutManager = LinearLayoutManager(context)

        // Configurar Adapter con el Launcher
        adapter = AssignmentAdapter(showCourseName = true) { assignment ->
            val token = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
            val intent = AssignmentDetailActivity.createIntent(
                context = requireContext(),
                assignment = assignment,
                fallbackCourseName = assignment.courseName,
                fallbackCourseColor = try { Color.parseColor(assignment.courseColor) } catch (e: Exception) { 0 },
                userToken = token
            )
            // En lugar de startActivity, usamos el launcher para esperar el resultado
            startDetailForResult.launch(intent)
        }
        rvAgenda.adapter = adapter

        viewModel = ViewModelProvider(this)[AgendaViewModel::class.java]

        // Observadores
        viewModel.cargando.observe(viewLifecycleOwner) { estaCargando ->
            swipeRefresh.isRefreshing = isUserRefreshing && estaCargando
            pbLoading.visibility = if (estaCargando && !isUserRefreshing) View.VISIBLE else View.GONE
            if (!estaCargando) {
                rvAgenda.visibility = View.VISIBLE
                isUserRefreshing = false
            }
        }

        viewModel.agenda.observe(viewLifecycleOwner) { tareas ->
            adapter.updateData(tareas)
            tvEmpty.visibility = if (tareas.isEmpty()) View.VISIBLE else View.GONE
        }

        swipeRefresh.setOnRefreshListener {
            isUserRefreshing = true
            ejecutarCarga()
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> viewModel.filtrarTodas()
                    R.id.chipNext7Days -> viewModel.filtrarProximos7Dias()
                    R.id.chipOverdue -> viewModel.filtrarAtrasadas()
                }
            }
        }

        ejecutarCarga()
    }

    private fun ejecutarCarga() {
        val token = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
        val userId = requireActivity().intent.getIntExtra("USER_ID", 0)

        if (token.isNotEmpty()) {
            // Obtenemos los IDs actualizados de la memoria local
            val completedIds = AssignmentProgressStore.getCompleted(requireContext())
            viewModel.cargarAgendaGlobal(token, userId, completedIds)
        }
    }
}