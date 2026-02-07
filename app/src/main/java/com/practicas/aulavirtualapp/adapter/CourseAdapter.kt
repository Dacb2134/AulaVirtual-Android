package com.practicas.aulavirtualapp.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.ColorGenerator
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.course.Course

// 👇 CAMBIO 1: Añadimos 'onCourseClick' al constructor
class CourseAdapter(
    private var courses: List<Course> = emptyList(),
    private val onCourseClick: (Course, Int) -> Unit // Función que recibe Curso y Color
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCourseName)
        val cvIcon: CardView = view.findViewById(R.id.cvIcon)
        val ivIcon: ImageView = cvIcon.getChildAt(0) as ImageView
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvPercent: TextView = view.findViewById(R.id.tvPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.tvName.text = course.fullName

        // Simulación visual
        val simulatedProgress = (course.id * 7) % 100
        holder.progressBar.progress = simulatedProgress
        holder.tvPercent.text = "$simulatedProgress%"

        val bgColor = ColorGenerator.getBackgroundColor(position)
        val iconColor = ColorGenerator.getIconColor(position)

        holder.cvIcon.setCardBackgroundColor(bgColor)
        holder.ivIcon.setColorFilter(iconColor)
        holder.progressBar.progressTintList = ColorStateList.valueOf(iconColor)
        holder.tvPercent.setTextColor(iconColor)

        // 👇 CAMBIO 2: Detectar el Clic
        holder.itemView.setOnClickListener {
            // Cuando tocan la tarjeta, ejecutamos la acción y le pasamos el curso y el color de fondo
            onCourseClick(course, iconColor)
        }

        // Animación
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation_fall_down)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount() = courses.size

    fun updateData(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    override fun onViewDetachedFromWindow(holder: CourseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }
}