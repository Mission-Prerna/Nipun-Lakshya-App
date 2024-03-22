package com.data.models.stateresult;

import java.io.Serializable;

public class ModuleResult implements Serializable {
    private String module;
    private Long startTime;
    private Long endTime;
    private Integer successCriteria;
    private Integer achievement;
    private Integer totalQuestions = 0;
    private boolean isPassed;
    private Integer stateGrade;
    private String extras;
    private Boolean sessionCompleted;
    private Integer appVersionCode;
    private String statement;
    private boolean isNetworkActive;

    public ModuleResult(String module, Integer successCriteria) {
        this.module = module;
        this.successCriteria = successCriteria;
    }

    public ModuleResult() {
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getSuccessCriteria() {
        return successCriteria;
    }

    public void setSuccessCriteria(Integer successCriteria) {
        this.successCriteria = successCriteria;
    }

    public Integer getAchievement() {
        return achievement;
    }

    public void setAchievement(Integer achievement) {
        this.achievement = achievement;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

    public Integer getStateGrade() {
        return stateGrade;
    }

    public void setStateGrade(Integer stateGrade) {
        this.stateGrade = stateGrade;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Boolean getSessionCompleted() {
        return sessionCompleted;
    }

    public void setSessionCompleted(Boolean sessionCompleted) {
        this.sessionCompleted = sessionCompleted;
    }

    public void setAppVersionCode(Integer appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public Integer getAppVersionCode() {
        return appVersionCode;
    }

    public boolean isNetworkActive() {
        return isNetworkActive;
    }

    public void setNetworkActive(boolean networkActive) {
        isNetworkActive = networkActive;
    }
}
