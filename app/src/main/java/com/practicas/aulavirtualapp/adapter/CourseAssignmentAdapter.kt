package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.assignment.Assignment

sealed class CourseAssignmentRow {
    data class Header(val title: String, val subtitle: String? = null) : CourseAssignmentRow()
    data class Item(
        val assignment: Assignment,
        val status: AssignmentStatus,
        val dueLabel: String,
        val isCompleted: Boolean
    ) : CourseAssignmentRow()
}

enum class AssignmentStatus {
    PENDING,
    OVERDUE,
    COMPLETED,
    NO_DATE
}

class CourseAssignmentAdapter(
    private val onAssignmentClick: (Assignment) -> Unit,
    private val onToggleCompletion: (Assignment, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<CourseAssignmentRow> = mutableListOf()

    fun submitItems(newItems: List<CourseAssignmentRow>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CourseAssignmentRow.Header -> VIEW_TYPE_HEADER
            is CourseAssignmentRow.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_course_assignment_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_course_assignment, parent, false)
                AssignmentViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CourseAssignmentRow.Header -> (holder as HeaderViewHolder).bind(item)
            is CourseAssignmentRow.Item -> (holder as AssignmentViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSectionSubtitle)

        fun bind(item: CourseAssignmentRow.Header) {
            tvTitle.text = item.title
            if (item.subtitle.isNullOrBlank()) {
                tvSubtitle.visibility = View.GONE
            } else {
                tvSubtitle.visibility = View.VISIBLE
                tvSubtitle.text = item.subtitle
            }
        }
    }

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvCourseAssignmentTitle)
        private val tvDue: TextView = itemView.findViewById(R.id.tvCourseAssignmentDue)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvCourseAssignmentStatus)
        private val btnDetail: MaterialButton = itemView.findViewById(R.id.btnCourseAssignmentDetail)
        private val btnComplete: MaterialButton = itemView.findViewById(R.id.btnCourseAssignmentComplete)

        fun bind(item: CourseAssignmentRow.Item) {
            val context = itemView.context
            tvTitle.text = item.assignment.name
            tvDue.text = item.dueLabel
            tvStatus.text = statusLabel(item.status)
            tvStatus.backgroundTintList = ContextCompat.getColorStateList(
                context,
                statusColor(item.status)
            )

            btnDetail.setOnClickListener { onAssignmentClick(item.assignment) }
            itemView.setOnClickListener { onAssignmentClick(item.assignment) }

            if (item.isCompleted) {
                btnComplete.text = "Desmarcar"
                btnComplete.setIconResource(R.drawable.ic_check)
            } else {
                btnComplete.text = "Marcar hecha"
                btnComplete.setIconResource(R.drawable.ic_check_outline)
            }

            btnComplete.setOnClickListener {
                onToggleCompletion(item.assignment, !item.isCompleted)
            }
        }

        private fun statusLabel(status: AssignmentStatus): String {
            return when (status) {
                AssignmentStatus.OVERDUE -> "Atrasada"
                AssignmentStatus.COMPLETED -> "Hecha"
                AssignmentStatus.NO_DATE -> "Sin fecha"
                AssignmentStatus.PENDING -> "Pendiente"
            }
        }

        private fun statusColor(status: AssignmentStatus): Int {
            return when (status) {
                AssignmentStatus.OVERDUE -> R.color.status_overdue
                AssignmentStatus.COMPLETED -> R.color.status_completed
                AssignmentStatus.NO_DATE -> R.color.status_neutral
                AssignmentStatus.PENDING -> R.color.status_pending
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
