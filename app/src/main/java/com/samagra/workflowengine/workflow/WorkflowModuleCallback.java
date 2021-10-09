package com.samagra.workflowengine.workflow;

import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

public interface WorkflowModuleCallback {
    void onSuccess(StateResult stateResult);
    void onFailure(StateResult stateResult);
}
