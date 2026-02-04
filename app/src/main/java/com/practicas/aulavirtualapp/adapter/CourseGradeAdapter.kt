package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.GradeItem

class CourseGradeAdapter(
    private var grades: List<GradeItem> = emptyList()
) : RecyclerView.Adapter<CourseGradeAdapter.GradeViewHolder>() {

    class GradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvGradeTitle)
        val tvScore: TextView = view.findViewById(R.id.tvGradeScore)
        val tvRange: TextView = view.findViewById(R.id.tvGradeRange)
        val tvWeight: TextView = view.findViewById(R.id.tvGradeWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_grade, parent, false)
        return GradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val grade = grades[position]
        val title = grade.itemName ?: "Actividad"
        val score = grade.gradeFormatted ?: grade.gradeRaw?.toString() ?: "Sin calificar"
        val min = grade.gradeMin?.toString() ?: "0"
        val max = grade.gradeMax?.toString() ?: "100"
        val percentage = grade.percentageFormatted ?: ""
        val weight = grade.weightFormatted ?: ""

        holder.tvTitle.text = title
        holder.tvScore.text = score
        holder.tvRange.text = "$min - $max $percentage".trim()
        holder.tvWeight.text = if (weight.isNotBlank()) "Peso: $weight" else ""
        holder.tvWeight.visibility = if (weight.isNotBlank()) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = grades.size

    fun updateData(newGrades: List<GradeItem>) {
        grades = newGrades
        notifyDataSetChanged()
    }
}
