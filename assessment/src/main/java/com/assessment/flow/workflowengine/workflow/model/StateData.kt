package com.assessment.flow.workflowengine.workflow.model


/**
 * This class is used to provide any type of data to any workflow module.
 * Add keys and use it on particular modules, make sure keys can be reused.
 */
data class StateData(
    @JvmField var formID:String = "",
    @JvmField var successCriteria:Int = 0
)
