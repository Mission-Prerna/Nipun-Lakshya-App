package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CorrectResponse {

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("outcomes")
    @Expose
    private Outcomes outcomes;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Outcomes getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(Outcomes outcomes) {
        this.outcomes = outcomes;
    }

}
