package com.assessment.studentselection.data

class StudentAssessmentHistory : ArrayList<StudentAssessmentHistoryItem>()

data class StudentAssessmentHistoryItem(
    val grade: Int,
    val period: String,
    val students: List<Student>,
    val summary: List<Summary>
)
data class Student(
    val id: String,
    val last_assessment_date: Long,
    val status: String
)

data class Summary(
    val colour: String,
    val count: Int,
    val label: String
)