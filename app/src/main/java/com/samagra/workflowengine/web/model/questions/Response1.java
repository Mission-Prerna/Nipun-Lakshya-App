package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response1 {

    @SerializedName("maxScore")
    @Expose
    private Integer maxScore;
    @SerializedName("cardinality")
    @Expose
    private String cardinality;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("correctResponse")
    @Expose
    private CorrectResponse correctResponse;

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CorrectResponse getCorrectResponse() {
        return correctResponse;
    }

    public void setCorrectResponse(CorrectResponse correctResponse) {
        this.correctResponse = correctResponse;
    }

}
