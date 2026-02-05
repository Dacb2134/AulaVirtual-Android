package com.practicas.aulavirtualapp.ui.assignmentdetail

import android.content.Intent

data class AssignmentDetailArgs(
    val title: String,
    val description: String,
    val courseName: String,
    val courseColor: Int,
    val dueDate: Long,
    val allowFrom: Long,
    val cutoffDate: Long,
    val gradingDueDate: Long,
    val maxAttempts: Int,
    val courseModuleId: Int,
    val assignmentId: Int,
    val allowFileSubmission: Boolean,
    val allowTextSubmission: Boolean,
    val fileExtensions: String,
    val userToken: String
) {
    companion object {
        private const val EXTRA_ASSIGNMENT_TITLE = "extra_assignment_title"
        private const val EXTRA_ASSIGNMENT_DESCRIPTION = "extra_assignment_description"
        private const val EXTRA_ASSIGNMENT_COURSE = "extra_assignment_course"
        private const val EXTRA_ASSIGNMENT_COURSE_COLOR = "extra_assignment_course_color"
        private const val EXTRA_ASSIGNMENT_DUE_DATE = "extra_assignment_due_date"
        private const val EXTRA_ASSIGNMENT_ALLOW_FROM = "extra_assignment_allow_from"
        private const val EXTRA_ASSIGNMENT_CUTOFF = "extra_assignment_cutoff"
        private const val EXTRA_ASSIGNMENT_GRADING_DUE = "extra_assignment_grading_due"
        private const val EXTRA_ASSIGNMENT_MAX_ATTEMPTS = "extra_assignment_max_attempts"
        private const val EXTRA_ASSIGNMENT_COURSE_MODULE_ID = "extra_assignment_course_module_id"
        private const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
        private const val EXTRA_ASSIGNMENT_ALLOW_FILES = "extra_assignment_allow_files"
        private const val EXTRA_ASSIGNMENT_ALLOW_TEXT = "extra_assignment_allow_text"
        private const val EXTRA_ASSIGNMENT_FILE_EXTENSIONS = "extra_assignment_file_extensions"
        private const val EXTRA_USER_TOKEN = "extra_user_token"

        fun fromIntent(intent: Intent): AssignmentDetailArgs {
            return AssignmentDetailArgs(
                title = intent.getStringExtra(EXTRA_ASSIGNMENT_TITLE).orEmpty(),
                description = intent.getStringExtra(EXTRA_ASSIGNMENT_DESCRIPTION).orEmpty(),
                courseName = intent.getStringExtra(EXTRA_ASSIGNMENT_COURSE).orEmpty(),
                courseColor = intent.getIntExtra(EXTRA_ASSIGNMENT_COURSE_COLOR, 0),
                dueDate = intent.getLongExtra(EXTRA_ASSIGNMENT_DUE_DATE, 0L),
                allowFrom = intent.getLongExtra(EXTRA_ASSIGNMENT_ALLOW_FROM, 0L),
                cutoffDate = intent.getLongExtra(EXTRA_ASSIGNMENT_CUTOFF, 0L),
                gradingDueDate = intent.getLongExtra(EXTRA_ASSIGNMENT_GRADING_DUE, 0L),
                maxAttempts = intent.getIntExtra(EXTRA_ASSIGNMENT_MAX_ATTEMPTS, 0),
                courseModuleId = intent.getIntExtra(EXTRA_ASSIGNMENT_COURSE_MODULE_ID, 0),
                assignmentId = intent.getIntExtra(EXTRA_ASSIGNMENT_ID, 0),
                allowFileSubmission = intent.getBooleanExtra(EXTRA_ASSIGNMENT_ALLOW_FILES, true),
                allowTextSubmission = intent.getBooleanExtra(EXTRA_ASSIGNMENT_ALLOW_TEXT, true),
                fileExtensions = intent.getStringExtra(EXTRA_ASSIGNMENT_FILE_EXTENSIONS).orEmpty(),
                userToken = intent.getStringExtra(EXTRA_USER_TOKEN).orEmpty()
            )
        }
    }
}
