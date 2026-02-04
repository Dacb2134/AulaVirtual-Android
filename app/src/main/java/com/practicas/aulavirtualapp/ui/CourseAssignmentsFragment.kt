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
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.AssignmentAdapter
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.CourseDetailViewModel

class CourseAssignmentsFragment : Fragment() {

    private lateinit var viewModel: CourseDetailViewModel
    private lateinit var adapter: AssignmentAdapter

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

        adapter = AssignmentAdapter(showCourseName = false) { assignment ->
            val intent = AssignmentDetailActivity.createIntent(
                requireContext(),
                assignment,
                arguments?.getString("COURSE_NAME").orEmpty(),
                arguments?.getInt("COURSE_COLOR") ?: 0
            )
            startActivity(intent)
        }
        rvAssignments.layoutManager = LinearLayoutManager(context)
        rvAssignments.adapter = adapter

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

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadAssignments(token, courseId)
        }

        viewModel.assignments.observe(viewLifecycleOwner) { assignments ->
            pbLoading.visibility = View.GONE
            val safeAssignments = assignments ?: emptyList()
            if (safeAssignments.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            } else {
                tvEmpty.visibility = View.GONE
                safeAssignments.forEach { assignment ->
                    assignment.courseName = courseName
                    assignment.courseColor = if (courseColor != 0) colorHex else assignment.courseColor
                }
                adapter.updateData(safeAssignments.sortedBy { it.dueDate ?: 0L })
            }
            swipeRefresh.isRefreshing = false
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
