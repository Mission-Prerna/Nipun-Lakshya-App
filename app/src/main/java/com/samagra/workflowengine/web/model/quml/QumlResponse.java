package com.samagra.workflowengine.web.model.quml;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QumlResponse {

    @SerializedName("index")
    @Expose
    private String index;
    @SerializedName("class")
    @Expose
    private String _class;
    @SerializedName("score")
    @Expose
    private Integer score;
    @SerializedName("isActive")
    @Expose
    private Boolean isActive;
    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("children")
    @Expose
    private List<Child> children = null;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

}
