package com.assessment.studentselection

import com.data.db.models.StudentAssessmentHistoryCompleteInfo

sealed class StudentAssessmentHistoryCompleteInfoStates {
    object Loading : StudentAssessmentHistoryCompleteInfoStates()
    class Error(val t: Throwable) : StudentAssessmentHistoryCompleteInfoStates()
    class Success(val studentsAssessmentHistoryCompleteInfo: StudentAssessmentHistoryCompleteInfo?) : StudentAssessmentHistoryCompleteInfoStates()
}