package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.Assignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentAdapter(private var assignments: List<Assignment> = emptyList()) :
    RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = assignments[position]
        holder.tvTitle.text = item.name

        // Truco: Convertir fecha de Moodle (Timestamp) a texto legible
        // Moodle envía segundos, Java usa milisegundos -> multiplicamos por 1000
        val date = Date(item.dueDate * 1000L)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvDate.text = "Vence: ${format.format(date)}"
    }

    override fun getItemCount() = assignments.size

    fun updateData(newList: List<Assignment>) {
        assignments = newList
        notifyDataSetChanged()
    }
}