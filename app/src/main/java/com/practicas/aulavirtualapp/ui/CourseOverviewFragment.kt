package com.practicas.aulavirtualapp.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.viewmodel.CourseContentViewModel

class CourseOverviewFragment : Fragment() {

    private lateinit var viewModel: CourseContentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvCourseName = view.findViewById<TextView>(R.id.tvOverviewCourseName)
        val tvCourseShort = view.findViewById<TextView>(R.id.tvOverviewCourseShort)
        val tvSummary = view.findViewById<TextView>(R.id.tvOverviewSummary)
        val tvSectionsCount = view.findViewById<TextView>(R.id.tvOverviewSectionsCount)
        val tvActivitiesCount = view.findViewById<TextView>(R.id.tvOverviewActivitiesCount)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbOverviewLoading)

        val courseName = arguments?.getString("COURSE_NAME") ?: "Curso"
        val courseShortName = arguments?.getString("COURSE_SHORT_NAME") ?: ""
        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")

        tvCourseName.text = courseName
        tvCourseShort.text = courseShortName.ifBlank { "Código no disponible" }

        viewModel = ViewModelProvider(requireActivity())[CourseContentViewModel::class.java]

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadCourseContents(token, courseId)
        }

        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            pbLoading.visibility = View.GONE
            updateOverview(tvSummary, tvSectionsCount, tvActivitiesCount, sections)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateOverview(
        tvSummary: TextView,
        tvSectionsCount: TextView,
        tvActivitiesCount: TextView,
        sections: List<CourseSection>
    ) {
        val visibleSections = sections.filter { (it.visible ?: 1) == 1 }
        val totalActivities = visibleSections.sumOf { it.modules.size }
        val summarySection = visibleSections.firstOrNull { !it.summary.isNullOrBlank() }
        val summaryText = summarySection?.summary?.takeIf { it.isNotBlank() } ?: "Sin descripción disponible."

        tvSectionsCount.text = visibleSections.size.toString()
        tvActivitiesCount.text = totalActivities.toString()
        tvSummary.text = Html.fromHtml(summaryText, Html.FROM_HTML_MODE_LEGACY)
    }
}
