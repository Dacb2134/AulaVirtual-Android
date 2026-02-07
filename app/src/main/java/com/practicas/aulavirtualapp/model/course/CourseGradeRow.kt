package com.practicas.aulavirtualapp.model.course

import com.practicas.aulavirtualapp.model.grades.GradeItem

sealed class CourseGradeRow {
    data class Category(val grade: GradeItem) : CourseGradeRow()
    data class Item(val grade: GradeItem, val categoryTitle: String?) : CourseGradeRow()
}