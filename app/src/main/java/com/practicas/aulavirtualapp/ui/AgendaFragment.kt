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
        // Inflamos el diseño que creamos para la agenda
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculamos las vistas del XML
        val rvAgenda = view.findViewById<RecyclerView>(R.id.rvAgenda)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoadingAgenda)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyAgenda)

        // Configuramos la lista (RecyclerView)
        rvAgenda.layoutManager = LinearLayoutManager(context)
        adapter = AssignmentAdapter() // Usamos el mismo adaptador que ya tenías
        rvAgenda.adapter = adapter

        // Inicializamos el ViewModel (El cerebro)
        viewModel = ViewModelProvider(this)[AgendaViewModel::class.java]

        // Recuperamos Token y ID del usuario desde la Actividad Principal
        val token = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
        // OJO: Si no has pasado el USER_ID en el Login, esto será 0 y podría fallar la carga.
        val userId = requireActivity().intent.getIntExtra("USER_ID", 0)

        // OBSERVADORES:
        //  estado de "Cargando"
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

        // La lista de tareas
        viewModel.agenda.observe(viewLifecycleOwner) { tareas ->
            if (tareas.isNotEmpty()) {
                adapter.updateData(tareas)
                tvEmpty.visibility = View.GONE
            } else {
                // Si la lista está vacía, mostramos el mensaje
                tvEmpty.text = "¡Todo al día! No hay tareas pendientes."
                tvEmpty.visibility = View.VISIBLE
            }
        }

        // Si hay algún mensaje de error
        viewModel.mensaje.observe(viewLifecycleOwner) { texto ->
            // Solo mostramos Toast si es un error real, no mensajes informativos
            if (texto.contains("Error") || texto.contains("Fallo")) {
                Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
            } else if (texto.contains("No tienes cursos")) {
                tvEmpty.text = texto
                tvEmpty.visibility = View.VISIBLE
            }
        }

        // carga de datos
        if (token.isNotEmpty() && userId != 0) {
            viewModel.cargarAgendaGlobal(token, userId)
        } else {
            if (userId == 0) {
                // Si sale este mensaje, es que falta poner el ID en el MainActivity (paso siguiente)
                tvEmpty.text = "Error: No se pudo identificar al usuario."
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }
}