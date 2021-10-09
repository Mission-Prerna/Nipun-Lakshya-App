package com.samagra.workflowengine.workflow.model;

import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.List;

public class Decision {
    private int id;
    private JsonElement meta;
    private List<Integer> successActions;
    private List<Integer> failureActions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JsonElement getMeta() {
        return meta;
    }

    public void setMeta(JsonElement meta) {
        this.meta = meta;
    }

    public List<Integer> getSuccessActions() {
        return successActions;
    }

    public void setSuccessActions(List<Integer> successActions) {
        this.successActions = successActions;
    }

    public List<Integer> getFailureActions() {
        return failureActions;
    }

    public void setFailureActions(List<Integer> failureActions) {
        this.failureActions = failureActions;
    }

    @Override
    public String toString() {
        return "Decision{" +
                ", successActions=" + Arrays.toString(successActions.toArray()) +
                ", failureActions=" + Arrays.toString(failureActions.toArray()) +
                '}';
    }
}
