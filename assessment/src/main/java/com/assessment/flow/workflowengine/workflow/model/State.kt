package com.assessment.flow.workflowengine.workflow.model

data class State(
    var id: Long,
    var subject: String,
    var gradeNumber: Int?,
    var type: String,
    var maxFailureCount: Int,
    var stateData: StateData,
    var decision: Decision
)