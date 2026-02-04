package com.practicas.aulavirtualapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.Assignment
import java.text.SimpleDateFormat
import java.util.*

class AssignmentAdapter(
    private var assignments: List<Assignment> = emptyList()
) : RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    private val headerFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun updateData(newAssignments: List<Assignment>) {
        this.assignments = newAssignments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val tareaActual = assignments[position]

        // --- LÓGICA DE AGRUPACIÓN (FECHAS) ---
        val fechaActualTexto = if (tareaActual.dueDate > 0) {
            headerFormat.format(Date(tareaActual.dueDate * 1000))
        } else {
            "Sin fecha"
        }
        var mostrarEncabezado = true

        if (position > 0) {
            val tareaAnterior = assignments[position - 1]
            val fechaAnteriorTexto = if (tareaAnterior.dueDate > 0) {
                headerFormat.format(Date(tareaAnterior.dueDate * 1000))
            } else {
                "Sin fecha"
            }
            if (fechaActualTexto == fechaAnteriorTexto) {
                mostrarEncabezado = false
            }
        }

        holder.bind(tareaActual, mostrarEncabezado, fechaActualTexto, timeFormat)
    }

    override fun getItemCount() = assignments.size

    class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvDateHeader)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAssignmentTitle)
        private val tvCourse: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val viewColor: View = itemView.findViewById(R.id.viewColorStrip)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(assignment: Assignment, showHeader: Boolean, dateText: String, timeFormat: SimpleDateFormat) {

            // 1. Encabezado de fecha
            if (showHeader) {
                tvHeader.visibility = View.VISIBLE
                tvHeader.text = dateText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } else {
                tvHeader.visibility = View.GONE
            }

            // 2. Datos básicos
            tvTitle.text = assignment.name
            tvCourse.text = if (assignment.courseName.isNotEmpty()) assignment.courseName else "Curso General"

            // 3. --- LÓGICA DE ADVERTENCIA DE TIEMPO (NUEVO) ---
            val now = System.currentTimeMillis()
            val dueDateMillis = assignment.dueDate * 1000
            val diff = dueDateMillis - now

            val unaHora = 60 * 60 * 1000
            val unDia = 24 * unaHora

            val fechaHoraTexto = if (assignment.dueDate > 0) {
                "Vence: ${timeFormat.format(Date(dueDateMillis))}"
            } else {
                "Sin fecha de entrega"
            }

            if (assignment.dueDate <= 0) {
                tvDate.text = fechaHoraTexto
                tvDate.setTextColor(Color.parseColor("#666666"))
            } else if (diff < 0) {
                // A) ATRASADA (Ya pasó la fecha)
                tvDate.text = "⚠ Atrasada ($fechaHoraTexto)"
                tvDate.setTextColor(Color.RED)
            } else if (diff < unDia) {
                // B) URGENTE (Menos de 24 horas)
                tvDate.text = "⚠ Expira pronto ($fechaHoraTexto)"
                // Color Naranja fuerte
                tvDate.setTextColor(Color.parseColor("#FF6D00"))
            } else {
                // C) NORMAL (Más de 24 horas)
                tvDate.text = fechaHoraTexto
                tvDate.setTextColor(Color.parseColor("#666666")) // Gris normal
            }

            // 4. Colores de la materia
            try {
                val color = Color.parseColor(assignment.courseColor)
                viewColor.setBackgroundColor(color)
                ivIcon.setColorFilter(color)
            } catch (e: Exception) {
                viewColor.setBackgroundColor(Color.parseColor("#6200EE"))
            }
        }
    }
}
