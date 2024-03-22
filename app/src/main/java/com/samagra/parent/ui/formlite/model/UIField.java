package com.samagra.parent.ui.formlite.model;

public class UIField {
    private String widget;
    private String key;
    private String label;
    private Object value;
    private Boolean visible;


    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Boolean isVisible() {
        if(visible == null){
            return true;
        }
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
