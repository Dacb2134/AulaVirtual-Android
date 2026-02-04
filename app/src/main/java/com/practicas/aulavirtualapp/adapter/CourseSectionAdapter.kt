package com.practicas.aulavirtualapp.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.CourseModule
import com.practicas.aulavirtualapp.model.CourseSection

class CourseSectionAdapter(
    private var sections: List<CourseSection> = emptyList()
) : RecyclerView.Adapter<CourseSectionAdapter.SectionViewHolder>() {

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSectionTitle: TextView = view.findViewById(R.id.tvSectionTitle)
        val tvSectionSummary: TextView = view.findViewById(R.id.tvSectionSummary)
        val tvSectionCount: TextView = view.findViewById(R.id.tvSectionCount)
        val modulesContainer: LinearLayout = view.findViewById(R.id.modulesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        val sectionNumber = section.section ?: position
        val title = if (!section.name.isNullOrBlank()) {
            section.name
        } else if (sectionNumber == 0) {
            "Presentación"
        } else {
            "Unidad $sectionNumber"
        }

        holder.tvSectionTitle.text = title
        holder.tvSectionCount.text = "${section.modules.size} actividades"

        val summaryText = section.summary?.takeIf { it.isNotBlank() } ?: "Sin descripción"
        holder.tvSectionSummary.text = Html.fromHtml(summaryText, Html.FROM_HTML_MODE_LEGACY)

        holder.modulesContainer.removeAllViews()
        section.modules.forEach { module ->
            holder.modulesContainer.addView(buildModuleView(holder.modulesContainer, module))
        }
    }

    override fun getItemCount(): Int = sections.size

    fun updateData(newSections: List<CourseSection>) {
        sections = newSections
        notifyDataSetChanged()
    }

    private fun buildModuleView(container: LinearLayout, module: CourseModule): View {
        val context = container.context
        val textView = TextView(context)
        val moduleType = module.modPlural ?: module.modName ?: "Actividad"
        val moduleName = module.name ?: moduleType
        textView.text = "• $moduleType: $moduleName"
        textView.setTextAppearance(android.R.style.TextAppearance_Material_Body2)
        textView.setPadding(0, 6, 0, 6)
        return textView
    }
}
