package com.practicas.aulavirtualapp.utils

import android.content.Context

object AssignmentProgressStore {
    private const val PREFS_NAME = "assignment_progress"
    private const val KEY_COMPLETED = "completed_ids"

    fun getCompleted(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_COMPLETED, emptySet()) ?: emptySet()
    }

    fun setCompleted(context: Context, assignmentId: Int, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val updated = getCompleted(context).toMutableSet()
        if (completed) {
            updated.add(assignmentId.toString())
        } else {
            updated.remove(assignmentId.toString())
        }
        prefs.edit().putStringSet(KEY_COMPLETED, updated).apply()
    }
}
