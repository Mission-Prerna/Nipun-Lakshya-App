package com.assessment.flow.assessment

import com.data.models.stateresult.AssessmentStateResult
import com.data.db.models.helper.AssessmentStateDetails

sealed class AssessmentFlowState {
    class Loading(val enabled : Boolean) : AssessmentFlowState()

    class Error(val t: Throwable) : AssessmentFlowState()

    object Completed : AssessmentFlowState()

    class Next(val state: AssessmentStateDetails) : AssessmentFlowState()

    class OnResult(val result : AssessmentStateResult) : AssessmentFlowState()

    class OnExit(val reason : String) : AssessmentFlowState()
}

