package com.data.db.models.helper

import androidx.room.ColumnInfo

data class SchoolAssessmentCount(
    @ColumnInfo(name = "total_students")
    val totalStudents: Int,
    @ColumnInfo(name = "total_non_pending")
    val totalNonPending: Int
)
