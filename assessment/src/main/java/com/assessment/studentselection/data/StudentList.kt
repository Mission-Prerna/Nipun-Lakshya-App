package com.assessment.studentselection.data

class StudentList : ArrayList<StudentListItem>()

data class StudentListItem(
    val grade: Int,
    val id: String,
    val name: String
)