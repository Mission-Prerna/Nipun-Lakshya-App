package com.samagra.workflowengine.workflow.model;

public class Action {
    private int id;
    private int count;
    private Types type;
    private int futureStateId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Types getType() {
        return type;
    }

    public void setType(Types type) {
        this.type = type;
    }

    public int getFutureStateId() {
        return futureStateId;
    }

    public void setFutureStateId(int futureStateId) {
        this.futureStateId = futureStateId;
    }

    public enum Types {
        CLEAR, RETRY, ADD_CURRENT, POP, PUSH
    }
}
