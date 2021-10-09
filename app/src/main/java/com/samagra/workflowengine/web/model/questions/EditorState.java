package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EditorState {

    @SerializedName("options")
    @Expose
    private List<Option> options = null;
    @SerializedName("question")
    @Expose
    private String question;

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

}
