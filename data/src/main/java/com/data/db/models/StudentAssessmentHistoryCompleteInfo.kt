package com.data.db.models

import com.data.db.models.entity.StudentAssessmentHistory

data class StudentAssessmentHistoryCompleteInfo(
    val grade: String,
    val period: String,
    val students: List<StudentAssessmentHistory>,
    val summary: List<Summary>
)