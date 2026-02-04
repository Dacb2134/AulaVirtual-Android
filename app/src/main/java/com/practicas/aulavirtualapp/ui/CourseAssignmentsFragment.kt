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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentStatus
import com.practicas.aulavirtualapp.adapter.CourseAssignmentAdapter
import com.practicas.aulavirtualapp.adapter.CourseAssignmentRow
import com.practicas.aulavirtualapp.model.Assignment
import com.practicas.aulavirtualapp.utils.AssignmentProgressStore
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.CourseDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CourseAssignmentsFragment : Fragment() {

    private lateinit var viewModel: CourseDetailViewModel
    private lateinit var adapter: CourseAssignmentAdapter
    private val dateFormat = SimpleDateFormat("dd MMM • HH:mm", Locale("es", "ES"))
    private var currentFilter: CourseFilter = CourseFilter.ALL
    private var cachedAssignments: List<Assignment> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_assignments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAssignments = view.findViewById<RecyclerView>(R.id.rvCourseAssignments)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbAssignmentsLoading)
        val tvEmpty = view.findViewById<TextView>(R.id.tvAssignmentsEmpty)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshAssignments)
        val tvPendingCount = view.findViewById<TextView>(R.id.tvPendingCount)
        val tvOverdueCount = view.findViewById<TextView>(R.id.tvOverdueCount)
        val tvCompletedCount = view.findViewById<TextView>(R.id.tvCompletedCount)

        val chipAll = view.findViewById<Chip>(R.id.chipCourseAll)
        val chipPending = view.findViewById<Chip>(R.id.chipCoursePending)
        val chipOverdue = view.findViewById<Chip>(R.id.chipCourseOverdue)
        val chipCompleted = view.findViewById<Chip>(R.id.chipCourseCompleted)

        adapter = CourseAssignmentAdapter(
            onAssignmentClick = { assignment ->
                val intent = AssignmentDetailActivity.createIntent(
                    requireContext(),
                    assignment,
                    arguments?.getString("COURSE_NAME").orEmpty(),
                    arguments?.getInt("COURSE_COLOR") ?: 0
                )
                startActivity(intent)
            },
            onToggleCompletion = { assignment, completed ->
                AssignmentProgressStore.setCompleted(requireContext(), assignment.id, completed)
                renderAssignments(tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount)
            }
        )

        rvAssignments.layoutManager = LinearLayoutManager(context)
        rvAssignments.adapter = adapter
        rvAssignments.setHasFixedSize(true)

        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")
        val courseName = arguments?.getString("COURSE_NAME").orEmpty()
        val courseColor = arguments?.getInt("COURSE_COLOR") ?: 0
        val colorHex = String.format("#%06X", 0xFFFFFF and courseColor)

        viewModel = ViewModelProvider(requireActivity())[CourseDetailViewModel::class.java]

        swipeRefresh.setupBrandColors()
        swipeRefresh.setOnRefreshListener {
            if (!token.isNullOrBlank() && courseId != 0) {
                viewModel.loadAssignments(token, courseId, forceRefresh = true)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }

        chipAll.setOnClickListener { updateFilter(CourseFilter.ALL, tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount) }
        chipPending.setOnClickListener { updateFilter(CourseFilter.PENDING, tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount) }
        chipOverdue.setOnClickListener { updateFilter(CourseFilter.OVERDUE, tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount) }
        chipCompleted.setOnClickListener { updateFilter(CourseFilter.COMPLETED, tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount) }

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadAssignments(token, courseId)
        }

        viewModel.assignments.observe(viewLifecycleOwner) { assignments ->
            pbLoading.visibility = View.GONE
            val safeAssignments = assignments ?: emptyList()
            if (safeAssignments.isEmpty()) {
                cachedAssignments = emptyList()
                adapter.submitItems(emptyList())
                tvEmpty.visibility = View.VISIBLE
            } else {
                safeAssignments.forEach { assignment ->
                    assignment.courseName = courseName
                    assignment.courseColor = if (courseColor != 0) colorHex else assignment.courseColor
                }
                cachedAssignments = safeAssignments.sortedBy { it.dueDate ?: 0L }
                renderAssignments(tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount)
            }
            swipeRefresh.isRefreshing = false
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateFilter(
        filter: CourseFilter,
        tvEmpty: TextView,
        tvPendingCount: TextView,
        tvOverdueCount: TextView,
        tvCompletedCount: TextView
    ) {
        currentFilter = filter
        renderAssignments(tvEmpty, tvPendingCount, tvOverdueCount, tvCompletedCount)
    }

    private fun renderAssignments(
        tvEmpty: TextView,
        tvPendingCount: TextView,
        tvOverdueCount: TextView,
        tvCompletedCount: TextView
    ) {
        val completedIds = AssignmentProgressStore.getCompleted(requireContext())
        val now = System.currentTimeMillis()

        val completed = cachedAssignments.filter { completedIds.contains(it.id.toString()) }
        val overdue = cachedAssignments.filter {
            !completedIds.contains(it.id.toString()) && (it.dueDate ?: 0L) > 0L && now > (it.dueDate ?: 0L) * 1000
        }
        val pendingWithDate = cachedAssignments.filter {
            !completedIds.contains(it.id.toString()) && (it.dueDate ?: 0L) > 0L && now <= (it.dueDate ?: 0L) * 1000
        }
        val pendingNoDate = cachedAssignments.filter {
            !completedIds.contains(it.id.toString()) && (it.dueDate ?: 0L) == 0L
        }

        tvPendingCount.text = (pendingWithDate.size + pendingNoDate.size).toString()
        tvOverdueCount.text = overdue.size.toString()
        tvCompletedCount.text = completed.size.toString()

        val items = mutableListOf<CourseAssignmentRow>()

        fun addSection(title: String, subtitle: String?, assignments: List<Assignment>, status: AssignmentStatus) {
            if (assignments.isNotEmpty()) {
                items.add(CourseAssignmentRow.Header(title, subtitle))
                assignments.forEach { assignment ->
                    items.add(
                        CourseAssignmentRow.Item(
                            assignment = assignment,
                            status = status,
                            dueLabel = buildDueLabel(assignment),
                            isCompleted = completedIds.contains(assignment.id.toString())
                        )
                    )
                }
            }
        }

        when (currentFilter) {
            CourseFilter.ALL -> {
                addSection("Atrasadas", "Necesitan tu atención inmediata", overdue, AssignmentStatus.OVERDUE)
                addSection("Pendientes", "Listas para entregar", pendingWithDate, AssignmentStatus.PENDING)
                addSection("Sin fecha", "Aún sin fecha de entrega", pendingNoDate, AssignmentStatus.NO_DATE)
                addSection("Hechas", "Entrega registrada", completed, AssignmentStatus.COMPLETED)
            }
            CourseFilter.PENDING -> {
                addSection("Pendientes", "Listas para entregar", pendingWithDate, AssignmentStatus.PENDING)
                addSection("Sin fecha", "Aún sin fecha de entrega", pendingNoDate, AssignmentStatus.NO_DATE)
            }
            CourseFilter.OVERDUE -> {
                addSection("Atrasadas", "Necesitan tu atención inmediata", overdue, AssignmentStatus.OVERDUE)
            }
            CourseFilter.COMPLETED -> {
                addSection("Hechas", "Entrega registrada", completed, AssignmentStatus.COMPLETED)
            }
        }

        adapter.submitItems(items)
        tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun buildDueLabel(assignment: Assignment): String {
        val dueDateSeconds = assignment.dueDate ?: 0L
        return if (dueDateSeconds > 0L) {
            "Vence: ${dateFormat.format(Date(dueDateSeconds * 1000))}"
        } else {
            "Vence: Sin fecha"
        }
    }

    private enum class CourseFilter {
        ALL,
        PENDING,
        OVERDUE,
        COMPLETED
    }
}
