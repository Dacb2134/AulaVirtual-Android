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
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.CourseSectionAdapter
import com.practicas.aulavirtualapp.viewmodel.CourseContentViewModel

class CourseContentFragment : Fragment() {

    private lateinit var viewModel: CourseContentViewModel
    private lateinit var adapter: CourseSectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSections = view.findViewById<RecyclerView>(R.id.rvCourseSections)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbCourseContentLoading)
        val tvEmpty = view.findViewById<TextView>(R.id.tvCourseContentEmpty)

        adapter = CourseSectionAdapter()
        rvSections.layoutManager = LinearLayoutManager(context)
        rvSections.adapter = adapter

        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")

        viewModel = ViewModelProvider(requireActivity())[CourseContentViewModel::class.java]

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadCourseContents(token, courseId)
        } else {
            pbLoading.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }

        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            pbLoading.visibility = View.GONE
            if (sections.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            } else {
                tvEmpty.visibility = View.GONE
                adapter.updateData(sections)
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
        }
    }
}
