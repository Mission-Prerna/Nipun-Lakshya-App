package com.samagra.parent.ui.assessmenthome.states

import com.data.db.models.TeacherPerformanceInsightsItem

sealed class TeacherInsightsStates {
    object Loading : TeacherInsightsStates()
    class Error(val t: Throwable) : TeacherInsightsStates()
    class Success(val teacherInsightsStatesInfo: List<TeacherPerformanceInsightsItem>) : TeacherInsightsStates()
}
