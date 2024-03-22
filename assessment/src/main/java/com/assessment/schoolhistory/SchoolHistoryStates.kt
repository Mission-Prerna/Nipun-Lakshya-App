package com.assessment.schoolhistory

import com.data.db.models.helper.AssessmentSchool


sealed class SchoolHistoryStates {
    object Loading : SchoolHistoryStates()
    class Error(val t: Throwable) : SchoolHistoryStates()
    class Success(val studentsAssessmentHistories: MutableList<AssessmentSchool>) : SchoolHistoryStates()
}