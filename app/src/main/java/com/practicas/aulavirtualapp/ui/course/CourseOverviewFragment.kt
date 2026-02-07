package com.practicas.aulavirtualapp.ui.course

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.course.CourseSection
import com.practicas.aulavirtualapp.utils.setupBrandColors
import com.practicas.aulavirtualapp.viewmodel.CourseContentViewModel
import com.practicas.aulavirtualapp.viewmodel.CourseParticipantsViewModel

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

        // ELIMINADO: val tvCourseShort = view.findViewById<TextView>(R.id.tvOverviewCourseShort) (No existe en tu XML)

        val tvSummary = view.findViewById<TextView>(R.id.tvOverviewSummary)
        val tvSectionsCount = view.findViewById<TextView>(R.id.tvOverviewSectionsCount)
        val tvActivitiesCount = view.findViewById<TextView>(R.id.tvOverviewActivitiesCount)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbOverviewLoading)
        val tvAssignmentsCount = view.findViewById<TextView>(R.id.tvOverviewAssignmentsCount)
        val tvForumsCount = view.findViewById<TextView>(R.id.tvOverviewForumsCount)
        val layoutActivityTypes = view.findViewById<LinearLayout>(R.id.layoutOverviewActivityTypes)
        val ivTeacherPhoto = view.findViewById<ImageView>(R.id.ivOverviewTeacherPhoto)
        val tvTeacherName = view.findViewById<TextView>(R.id.tvOverviewTeacherName)
        val tvTeacherRole = view.findViewById<TextView>(R.id.tvOverviewTeacherRole)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshOverview)

        val courseShortName = arguments?.getString("COURSE_SHORT_NAME") ?: ""
        val courseId = arguments?.getInt("COURSE_ID") ?: 0
        val token = arguments?.getString("USER_TOKEN")

        // ELIMINADO: tvCourseShort.text = ...

        viewModel = ViewModelProvider(requireActivity())[CourseContentViewModel::class.java]
        participantsViewModel = ViewModelProvider(requireActivity())[CourseParticipantsViewModel::class.java]

        swipeRefresh.setupBrandColors()
        swipeRefresh.setOnRefreshListener {
            if (!token.isNullOrBlank() && courseId != 0) {
                viewModel.loadCourseContents(token, courseId, forceRefresh = true)
                participantsViewModel.loadTeachers(token, courseId)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }

        if (!token.isNullOrBlank() && courseId != 0) {
            pbLoading.visibility = View.VISIBLE
            viewModel.loadCourseContents(token, courseId)
            participantsViewModel.loadTeachers(token, courseId)
        }

        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            pbLoading.visibility = View.GONE
            updateOverview(
                tvSummary = tvSummary,
                tvSectionsCount = tvSectionsCount,
                tvActivitiesCount = tvActivitiesCount,
                tvAssignmentsCount = tvAssignmentsCount,
                tvForumsCount = tvForumsCount,
                layoutActivityTypes = layoutActivityTypes,
                sections = sections
            )
            swipeRefresh.isRefreshing = false
        }

        participantsViewModel.teachers.observe(viewLifecycleOwner) { teachers ->
            val teacher = teachers.firstOrNull()
            if (teacher != null) {
                tvTeacherName.text = teacher.fullName ?: "Docente sin nombre"
                val roleName = teacher.roles.firstOrNull()?.name ?: "Docente"
                tvTeacherRole.text = roleName
                if (!teacher.profileImageUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(teacher.profileImageUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .circleCrop()
                        .into(ivTeacherPhoto)
                } else {
                    ivTeacherPhoto.setImageResource(R.drawable.ic_baseline_person_24)
                }
            } else {
                tvTeacherName.text = "Docente por confirmar"
                tvTeacherRole.text = "Sin asignar"
                ivTeacherPhoto.setImageResource(R.drawable.ic_baseline_person_24)
            }
        }

        participantsViewModel.message.observe(viewLifecycleOwner) { message ->
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            pbLoading.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateOverview(
        tvSummary: TextView,
        tvSectionsCount: TextView,
        tvActivitiesCount: TextView,
        tvAssignmentsCount: TextView,
        tvForumsCount: TextView,
        layoutActivityTypes: LinearLayout,
        sections: List<CourseSection>
    ) {
        val visibleSections = sections.filter { (it.visible ?: 1) == 1 }
        val visibleModules = visibleSections.flatMap { section ->
            section.modules.filter { (it.visible ?: 1) == 1 }
        }
        val totalActivities = visibleModules.size
        val assignmentCount = visibleModules.count { it.modName == "assign" }
        val forumCount = visibleModules.count { it.modName == "forum" }
        val summarySection = visibleSections.firstOrNull { !it.summary.isNullOrBlank() }
        val summaryText = summarySection?.summary?.takeIf { it.isNotBlank() } ?: "Sin descripción disponible."

        tvSectionsCount.text = visibleSections.size.toString()
        tvActivitiesCount.text = totalActivities.toString()
        tvAssignmentsCount.text = assignmentCount.toString()
        tvForumsCount.text = forumCount.toString()
        tvSummary.text = Html.fromHtml(summaryText, Html.FROM_HTML_MODE_LEGACY)

        val activityTypeCounts = visibleModules.groupingBy { module ->
            module.modPlural ?: module.modName ?: "Actividad"
        }.eachCount()

        layoutActivityTypes.removeAllViews()
        if (activityTypeCounts.isEmpty()) {
            val emptyView = TextView(layoutActivityTypes.context)
            emptyView.text = "Sin actividades registradas en Moodle."
            emptyView.setTextColor(ContextCompat.getColor(layoutActivityTypes.context, R.color.text_secondary))
            layoutActivityTypes.addView(emptyView)
        } else {
            activityTypeCounts.entries.sortedByDescending { it.value }.forEach { entry ->
                val textView = TextView(layoutActivityTypes.context)
                textView.text = "• ${entry.key}: ${entry.value}"
                textView.setTextAppearance(android.R.style.TextAppearance_Material_Body2)
                textView.setPadding(0, 6, 0, 6)
                layoutActivityTypes.addView(textView)
            }
        }
    }
}