package com.practicas.aulavirtualapp.ui

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
import com.google.android.material.chip.ChipGroup // Importante
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter
import com.practicas.aulavirtualapp.viewmodel.AgendaViewModel

class AgendaFragment : Fragment() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var viewModel: AgendaViewModel

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

        // Obtenemos referencia al grupo de Chips
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilters)

        rvAgenda.layoutManager = LinearLayoutManager(context)
        adapter = AssignmentAdapter()
        rvAgenda.adapter = adapter

        viewModel = ViewModelProvider(this)[AgendaViewModel::class.java]

        val token = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
        val userId = requireActivity().intent.getIntExtra("USER_ID", 0)

        // ESCUCHAMOS LOS CLICS EN LOS FILTROS
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> viewModel.filtrarTodas()
                    R.id.chipNext7Days -> viewModel.filtrarProximos7Dias()
                    R.id.chipOverdue -> viewModel.filtrarAtrasadas()
                }
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { estaCargando ->
            if (estaCargando) {
                pbLoading.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                rvAgenda.visibility = View.GONE
            } else {
                pbLoading.visibility = View.GONE
                rvAgenda.visibility = View.VISIBLE
            }
        }

        viewModel.agenda.observe(viewLifecycleOwner) { tareas ->
            if (tareas.isNotEmpty()) {
                adapter.updateData(tareas)
                tvEmpty.visibility = View.GONE
            } else {
                tvEmpty.text = "No se encontraron tareas con este filtro."
                tvEmpty.visibility = View.VISIBLE
                // Aunque esté vacío, actualizamos el adapter para limpiar la lista
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

        if (token.isNotEmpty()) {

            viewModel.cargarAgendaGlobal(token, userId)
        } else {
            tvEmpty.text = "Error: Sesión no válida."
            tvEmpty.visibility = View.VISIBLE
        }
    }
}