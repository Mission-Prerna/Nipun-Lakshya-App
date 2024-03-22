package com.assessment.studentselection

import com.data.db.models.helper.StudentWithAssessmentHistory


sealed class StudentAssessmentHistoryStates {
    object Loading : StudentAssessmentHistoryStates()
    class Error(val t: Throwable) : StudentAssessmentHistoryStates()
    class Success(val studentsAssessmentHistory: MutableList<StudentWithAssessmentHistory>) : StudentAssessmentHistoryStates()
}