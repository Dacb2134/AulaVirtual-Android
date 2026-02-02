package com.practicas.aulavirtualapp.ui // Fíjate que ahora dice .ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter // Importamos el adaptador desde su nueva casa

class AgendaFragment : Fragment() {

    private lateinit var adapter: AssignmentAdapter

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

        // Configurar la lista
        rvAgenda.layoutManager = LinearLayoutManager(context)

        // Reutilizamos el adaptador de tareas
        adapter = AssignmentAdapter()
        rvAgenda.adapter = adapter

        // TODO: Aquí conectaremos el ViewModel más adelante
        // Por ahora, simulamos que está vacío para ver el diseño
        pbLoading.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
    }
}