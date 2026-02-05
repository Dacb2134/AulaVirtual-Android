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
import com.practicas.aulavirtualapp.adapter.CourseGradeAdapter
import com.practicas.aulavirtualapp.model.CourseGradeRow
import com.practicas.aulavirtualapp.model.GradeItem
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.CourseGradesViewModel

class CourseGradesFragment : Fragment() {

    private lateinit var viewModel: CourseGradesViewModel
    private lateinit var adapter: CourseGradeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_grades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvGrades = view.findViewById<RecyclerView>(R.id.rvCourseGrades)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbGradesLoading)
        val tvEmpty = view.findViewById<TextView>(R.id.tvGradesEmpty)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshGrades)

        adapter = CourseGradeAdapter()
        rvGrades.layoutManager = LinearLayoutManager(context)
        rvGrades.adapter = adapter

        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")
        val userId = arguments?.getInt("USER_ID") ?: 0

        viewModel = ViewModelProvider(requireActivity())[CourseGradesViewModel::class.java]

        swipeRefresh.setupBrandColors()
        swipeRefresh.setOnRefreshListener {
            if (!token.isNullOrBlank() && courseId != 0 && userId != 0) {
                viewModel.loadGrades(token, courseId, userId)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }

        if (!token.isNullOrBlank() && courseId != 0 && userId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadGrades(token, courseId, userId)
        } else {
            pbLoading.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }

        viewModel.grades.observe(viewLifecycleOwner) { grades ->
            pbLoading.visibility = View.GONE
            if (grades.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            } else {
                tvEmpty.visibility = View.GONE
                adapter.updateData(buildGradeRows(grades))
            }
            swipeRefresh.isRefreshing = false
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun buildGradeRows(grades: List<GradeItem>): List<CourseGradeRow> {
        val rows = mutableListOf<CourseGradeRow>()
        var currentCategory: String? = null
        grades.forEach { grade ->
            when (grade.itemType?.lowercase()) {
                "category" -> {
                    currentCategory = grade.itemName ?: "Categoría"
                    rows.add(CourseGradeRow.Category(grade))
                }
                "course" -> {
                    rows.add(CourseGradeRow.Item(grade, "Total del curso"))
                }
                else -> {
                    rows.add(CourseGradeRow.Item(grade, currentCategory))
                }
            }
        }
        return rows
    }
}
