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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val VIEW_TYPE_GRADE = 0
        const val VIEW_TYPE_CATEGORY = 1
    }

    class GradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvGradeTitle)
        val tvType: TextView = view.findViewById(R.id.tvGradeType)
        val tvScore: TextView = view.findViewById(R.id.tvGradeScore)
        val tvRange: TextView = view.findViewById(R.id.tvGradeRange)
        val tvWeight: TextView = view.findViewById(R.id.tvGradeWeight)
    }

    class GradeCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvGradeCategoryTitle)
        val tvWeight: TextView = view.findViewById(R.id.tvGradeCategoryWeight)
    }

    override fun getItemViewType(position: Int): Int {
        val itemType = grades[position].itemType?.lowercase()
        return if (itemType == "category") VIEW_TYPE_CATEGORY else VIEW_TYPE_GRADE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CATEGORY) {
            val view = inflater.inflate(R.layout.item_course_grade_header, parent, false)
            GradeCategoryViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_course_grade, parent, false)
            GradeViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val grade = grades[position]
        val title = grade.itemName ?: "Actividad"
        val weight = grade.weightFormatted ?: ""

        if (holder is GradeCategoryViewHolder) {
            holder.tvTitle.text = title
            holder.tvWeight.text = if (weight.isNotBlank()) "Peso total: $weight" else "Peso total: -"
        } else if (holder is GradeViewHolder) {
            val score = grade.gradeFormatted ?: grade.gradeRaw?.toString() ?: "Sin calificar"
            val min = grade.gradeMin?.toString() ?: "0"
            val max = grade.gradeMax?.toString() ?: "100"
            val percentage = grade.percentageFormatted ?: ""
            val typeLabel = when (grade.itemType?.lowercase()) {
                "mod" -> "Actividad evaluada"
                "course" -> "Total del curso"
                "category" -> "Categoría"
                else -> "Evaluación"
            }

            holder.tvTitle.text = title
            holder.tvType.text = typeLabel
            holder.tvScore.text = score
            holder.tvRange.text = "$min - $max $percentage".trim()
            holder.tvWeight.text = if (weight.isNotBlank()) "Peso: $weight" else ""
            holder.tvWeight.visibility = if (weight.isNotBlank()) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int = grades.size

    fun updateData(newGrades: List<GradeItem>) {
        grades = newGrades
        notifyDataSetChanged()
    }
}
