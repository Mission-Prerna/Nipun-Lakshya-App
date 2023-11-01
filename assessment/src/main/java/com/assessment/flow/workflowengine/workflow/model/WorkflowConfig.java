package com.assessment.flow.workflowengine.workflow.model;

import com.samagra.commons.models.chaptersdata.ChapterMapping;

import java.util.List;

public class WorkflowConfig {

    private List<FlowConfig> flowConfigs;
    private List<Action> actions;
    private List<State> states;
    private List<ChapterMapping> chapterMapping;

    public List<FlowConfig> getFlowConfigs() {
        return flowConfigs;
    }

    public void setFlowConfigs(List<FlowConfig> flowConfigs) {
        this.flowConfigs = flowConfigs;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public List<ChapterMapping> getChapterMapping() {
        return chapterMapping;
    }

    public void setChapterMapping(List<ChapterMapping> chapterMapping) {
        this.chapterMapping = chapterMapping;
    }
}
