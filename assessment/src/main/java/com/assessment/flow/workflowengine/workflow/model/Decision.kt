package com.assessment.flow.workflowengine.workflow.model

import com.google.gson.JsonElement

data class Decision(
    var id: Int,
    var meta: JsonElement,
    var successActions: List<Int>,
    var failureActions: List<Int>
)