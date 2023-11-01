package com.samagra.parent.ui.competencyselection

import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import java.io.Serializable

data class StudentsAssessmentData(
    var viewType: String,
    var studentResults: AssessmentStateResult,
    var studentResultsOdk: AssessmentStateResult? = null
) : Serializable
