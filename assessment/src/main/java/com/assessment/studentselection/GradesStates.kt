package com.assessment.studentselection

sealed class GradesStates {
    object Loading : GradesStates()
    class Error(val t: Throwable) : GradesStates()
    class Success(val gradesList: List<Int>) : GradesStates()
}
