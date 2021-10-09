package com.samagra.workflowengine.workflow;

import java.util.Date;

public abstract class WorkFlow<T,P> {

    public WorkflowModuleCallback callback;
    public Date startTime;

    public WorkFlow(WorkflowModuleCallback callback) {
        this.callback = callback;
        startTime = new Date();
        handleCallbacks();
    }

    public abstract void onStart(T t);

    public abstract void setProps(P props);

    public abstract void handleCallbacks();
}