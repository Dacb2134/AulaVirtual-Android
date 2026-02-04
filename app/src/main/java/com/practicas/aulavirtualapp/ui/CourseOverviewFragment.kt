package com.practicas.aulavirtualapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.CourseSection
import com.practicas.aulavirtualapp.model.EnrolledUser
import com.practicas.aulavirtualapp.viewmodel.CourseParticipantsViewModel
import com.practicas.aulavirtualapp.viewmodel.CourseContentViewModel
import com.bumptech.glide.Glide

class CourseOverviewFragment : Fragment() {

    private lateinit var viewModel: CourseContentViewModel
    private lateinit var participantsViewModel: CourseParticipantsViewModel

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
        val ivTeacher = view.findViewById<ImageView>(R.id.ivOverviewTeacher)
        val tvTeacherName = view.findViewById<TextView>(R.id.tvOverviewTeacherName)
        val tvTeacherRole = view.findViewById<TextView>(R.id.tvOverviewTeacherRole)
        val highlightsContainer = view.findViewById<LinearLayout>(R.id.highlightsContainer)
        val tvHighlightsEmpty = view.findViewById<TextView>(R.id.tvOverviewHighlightsEmpty)

        val courseName = arguments?.getString("COURSE_NAME") ?: "Curso"
        val courseShortName = arguments?.getString("COURSE_SHORT_NAME") ?: ""
        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")

        tvCourseName.text = courseName
        tvCourseShort.text = courseShortName.ifBlank { "Código no disponible" }

        viewModel = ViewModelProvider(requireActivity())[CourseContentViewModel::class.java]
        participantsViewModel = ViewModelProvider(requireActivity())[CourseParticipantsViewModel::class.java]

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadCourseContents(token, courseId)
            participantsViewModel.loadTeachers(token, courseId)
        }

        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            pbLoading.visibility = View.GONE
            updateOverview(
                tvSummary,
                tvSectionsCount,
                tvActivitiesCount,
                highlightsContainer,
                tvHighlightsEmpty,
                sections
            )
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
        }

        participantsViewModel.teachers.observe(viewLifecycleOwner) { teachers ->
            updateTeacherCard(ivTeacher, tvTeacherName, tvTeacherRole, teachers)
        }

        participantsViewModel.message.observe(viewLifecycleOwner) { message ->
            context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun updateOverview(
        tvSummary: TextView,
        tvSectionsCount: TextView,
        tvActivitiesCount: TextView,
        highlightsContainer: LinearLayout,
        tvHighlightsEmpty: TextView,
        sections: List<CourseSection>
    ) {
        val visibleSections = sections.filter { (it.visible ?: 1) == 1 }
        val visibleModules = visibleSections.flatMap { section ->
            section.modules.filter { (it.visible ?: 1) == 1 }
        }
        val totalActivities = visibleModules.size
        val summarySection = visibleSections.firstOrNull { !it.summary.isNullOrBlank() }
        val summaryText = summarySection?.summary?.takeIf { it.isNotBlank() } ?: "Sin descripción disponible."

        tvSectionsCount.text = visibleSections.size.toString()
        tvActivitiesCount.text = totalActivities.toString()
        tvSummary.text = android.text.Html.fromHtml(summaryText, android.text.Html.FROM_HTML_MODE_LEGACY)

        highlightsContainer.removeAllViews()
        val highlights = visibleModules.take(5)
        if (highlights.isEmpty()) {
            tvHighlightsEmpty.visibility = View.VISIBLE
        } else {
            tvHighlightsEmpty.visibility = View.GONE
            highlights.forEach { module ->
                val itemView = LayoutInflater.from(highlightsContainer.context)
                    .inflate(R.layout.item_course_highlight, highlightsContainer, false)
                val tvTitle = itemView.findViewById<TextView>(R.id.tvHighlightTitle)
                val tvType = itemView.findViewById<TextView>(R.id.tvHighlightType)
                tvTitle.text = module.name ?: module.modPlural ?: "Actividad"
                tvType.text = module.modPlural ?: module.modName ?: "Actividad"
                highlightsContainer.addView(itemView)
            }
        }
    }

    private fun updateTeacherCard(
        ivTeacher: ImageView,
        tvTeacherName: TextView,
        tvTeacherRole: TextView,
        teachers: List<EnrolledUser>
    ) {
        val teacher = teachers.firstOrNull()
        tvTeacherName.text = teacher?.fullName ?: "Docente por confirmar"
        tvTeacherRole.text = teacher?.roles?.firstOrNull()?.name ?: "Docente"

        val imageUrl = teacher?.profileImageUrl
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(ivTeacher)
                .load(imageUrl)
                .placeholder(R.drawable.img_avatar_frame)
                .circleCrop()
                .into(ivTeacher)
        } else {
            ivTeacher.setImageResource(R.drawable.img_avatar_frame)
        }
    }
}
