package com.assessment.flow.workflowengine.workflow;


import com.data.models.stateresult.StateResult;

public interface WorkflowModuleCallback {
    void onSuccess(StateResult stateResult);
    void onFailure(StateResult stateResult);
}
