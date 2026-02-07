package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.course.CourseGradeRow

class CourseGradeAdapter(
    private var rows: List<CourseGradeRow> = emptyList()
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
        return when (rows[position]) {
            is CourseGradeRow.Category -> VIEW_TYPE_CATEGORY
            is CourseGradeRow.Item -> VIEW_TYPE_GRADE
        }
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
        val row = rows[position]
        val grade = when (row) {
            is CourseGradeRow.Category -> row.grade
            is CourseGradeRow.Item -> row.grade
        }
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
            val categoryLabel = (row as? CourseGradeRow.Item)?.categoryTitle
            val typeWithCategory = if (!categoryLabel.isNullOrBlank() && grade.itemType?.lowercase() != "course") {
                "$typeLabel · $categoryLabel"
            } else {
                typeLabel
            }

            holder.tvTitle.text = title
            holder.tvType.text = typeWithCategory
            holder.tvScore.text = score
            holder.tvRange.text = "$min - $max $percentage".trim()
            holder.tvWeight.text = if (weight.isNotBlank()) "Peso: $weight" else ""
            holder.tvWeight.visibility = if (weight.isNotBlank()) View.VISIBLE else View.GONE

            applyIndent(holder.itemView, categoryLabel != null && grade.itemType?.lowercase() != "course")
        }
    }

    override fun getItemCount(): Int = rows.size

    fun updateData(newRows: List<CourseGradeRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    private fun applyIndent(view: View, indented: Boolean) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val startMargin = if (indented) {
            (view.resources.displayMetrics.density * 12).toInt()
        } else {
            (view.resources.displayMetrics.density * 0).toInt()
        }
        if (params.marginStart != startMargin) {
            params.marginStart = startMargin
            view.layoutParams = params
        }
    }
}
