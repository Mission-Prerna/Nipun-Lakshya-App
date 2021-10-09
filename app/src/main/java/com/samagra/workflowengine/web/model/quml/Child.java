package com.samagra.workflowengine.web.model.quml;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Child {

    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("index")
    @Expose
    private Integer index;
    @SerializedName("class")
    @Expose
    private String _class;
    @SerializedName("option")
    @Expose
    private Option option;
    @SerializedName("cardinality")
    @Expose
    private String cardinality;
    @SerializedName("score")
    @Expose
    private Integer score;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

}
