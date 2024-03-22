package com.samagra.parent.ui.assessmenthome.states

import com.data.db.models.MentorPerformanceInsightsItem

sealed class MentorInsightsStates {
    object Loading : MentorInsightsStates()
    class Error(val t: Throwable) : MentorInsightsStates()
    class Success(val mentorInsightsStatesInfo: MentorPerformanceInsightsItem) : MentorInsightsStates()
}
