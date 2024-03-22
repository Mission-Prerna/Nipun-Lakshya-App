package com.samagra.workflowengine.workflow;

import androidx.fragment.app.DialogFragment;

public abstract class WorkflowBaseFragment extends DialogFragment {

    protected WorkflowModuleCallback callback;

    protected WorkflowBaseFragment(WorkflowModuleCallback callback){
        this.callback = callback;
    }
}
