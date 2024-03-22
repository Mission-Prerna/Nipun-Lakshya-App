package com.samagra.parent.ui.assessmenthome.states

import com.data.db.models.ExaminerPerformanceInsightsItem

sealed class ExaminerInsightsStates {
    object Loading : ExaminerInsightsStates()
    class Error(val error: Throwable, val ctaEnable: Boolean = true) : ExaminerInsightsStates()
    class Success(val examinerInsightsStatesInfo: List<ExaminerPerformanceInsightsItem>) : ExaminerInsightsStates()
}
