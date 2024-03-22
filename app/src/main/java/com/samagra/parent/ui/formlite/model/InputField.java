package com.samagra.parent.ui.formlite.model;

import java.util.List;

public class InputField extends UIField {
    private Boolean required;
    private String text;
    private String eligibleDigits;
    private Boolean canEdit;
    private String validation;
    private List<DropdownOption> options;
    private ButtonAction action;
    private ActionResult result;
    private String inputType;
    private String errorMessage;
    private Integer maxLength;
    private String placeholder;
    private String validationMessage;

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEligibleDigits() {
        return eligibleDigits;
    }

    public void setEligibleDigits(String eligibleDigits) {
        this.eligibleDigits = eligibleDigits;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public List<DropdownOption> getOptions() {
        return options;
    }

    public void setOptions(List<DropdownOption> options) {
        this.options = options;
    }

    public ButtonAction getAction() {
        return action;
    }

    public void setAction(ButtonAction action) {
        this.action = action;
    }

    public ActionResult getResult() {
        return result;
    }

    public void setResult(ActionResult result) {
        this.result = result;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
