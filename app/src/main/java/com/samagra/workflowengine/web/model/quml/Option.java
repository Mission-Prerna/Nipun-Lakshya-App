package com.samagra.workflowengine.web.model.quml;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Option {

    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("value")
    @Expose
    private Integer value;
    @SerializedName("selected")
    @Expose
    private Boolean selected;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

}
