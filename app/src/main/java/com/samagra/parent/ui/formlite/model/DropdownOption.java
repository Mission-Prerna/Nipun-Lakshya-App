package com.samagra.parent.ui.formlite.model;

import java.util.List;

public class DropdownOption {
    private Object value;
    private String label;
    private boolean isPlaceHolder = false;
    private List<FieldSelections> fieldSelections;

    public DropdownOption(Object value, String label) {
        this.value = value;
        this.label = label;
        isPlaceHolder = true;
    }

    public DropdownOption() {
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<FieldSelections> getFieldSelections() {
        return fieldSelections;
    }

    public void setFieldSelections(List<FieldSelections> fieldSelections) {
        this.fieldSelections = fieldSelections;
    }

    @Override
    public String toString() {
        return label;
    }

    public boolean isPlaceHolder() {
        return isPlaceHolder;
    }

    public void setPlaceHolder(boolean placeHolder) {
        isPlaceHolder = placeHolder;
    }
}
