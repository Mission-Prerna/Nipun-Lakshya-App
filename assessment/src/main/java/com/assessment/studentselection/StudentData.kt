package com.assessment.studentselection

import java.io.Serializable

data class StudentData(val studentName: String, val studentLastAssessmentDate: String, val studentNipunStatus: Int) : Serializable
