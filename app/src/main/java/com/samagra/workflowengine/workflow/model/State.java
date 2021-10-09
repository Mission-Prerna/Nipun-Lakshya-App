package com.samagra.workflowengine.workflow.model;

import com.google.gson.annotations.SerializedName;
/*

public class State {
    private long id;
    private String subject;
    private String gradeNumber;
    private String type;
    private int maxFailureCount;

    private StateData stateData;

    private Decision decision;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGradeNumber() {
        return gradeNumber;
    }

    public void setGradeNumber(String gradeNumber) {
        this.gradeNumber = gradeNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StateData getStateData() {
        return stateData;
    }

    public void setStateData(StateData stateData) {
        this.stateData = stateData;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public int getMaxFailureAllowed() {
        return maxFailureCount;
    }

    public void setMaxFailureCount(int maxFailureCount) {
        this.maxFailureCount = maxFailureCount;
    }
}
*/

public class State {
    private long id;
    private String subject;
    private Integer gradeNumber;
    private String type;
    private int maxFailureCount;
    private StateData stateData;
    private Decision decision;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getGradeNumber() {
        return gradeNumber;
    }

    public void setGradeNumber(Integer gradeNumber) {
        this.gradeNumber = gradeNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StateData getStateData() {
        return stateData;
    }

    public void setStateData(StateData stateData) {
        this.stateData = stateData;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public int getMaxFailureAllowed() {
        return maxFailureCount;
    }

    public void setMaxFailureCount(int maxFailureCount) {
        this.maxFailureCount = maxFailureCount;
    }

    @Override
    public String toString() {
        return "State{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", gradeNumber='" + gradeNumber + '\'' +
                ", type='" + type + '\'' +
                ", maxFailureCount=" + maxFailureCount +
                ", stateData=" + stateData +
                ", decision=" + decision +
                '}';
    }
}




