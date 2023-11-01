package com.assessment.studentselection

import com.data.db.models.entity.Student
import com.data.db.models.helper.SchoolAssessmentCount
import com.data.db.models.helper.StudentWithAssessmentHistory

sealed class StudentScreenStates {
    object Loading : StudentScreenStates()
    class Error(val t: Throwable) : StudentScreenStates()
    class Success(val studentList: MutableList<Student>) : StudentScreenStates()
    class LoadMetricsData(val countText: String) : StudentScreenStates()
    class OpenSchoolSubmissionDisclaimer(val list: List<StudentWithAssessmentHistory>) :
        StudentScreenStates()
    object StartSchoolSubmissionFlow : StudentScreenStates()
    class ShowMessage(val msg: String) : StudentScreenStates()
    object OpenSchoolReport : StudentScreenStates()

}