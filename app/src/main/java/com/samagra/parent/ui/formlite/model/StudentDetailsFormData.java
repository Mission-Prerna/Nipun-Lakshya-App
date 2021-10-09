package com.samagra.parent.ui.formlite.model;

import java.util.List;

public class StudentDetailsFormData {
    private List<InputField> formFields;
    private Localiation localization;
    private List<InputField> additionalFields;

    public List<InputField> getFormFields() {
        return formFields;
    }

    public void setFormFields(List<InputField> formFields) {
        this.formFields = formFields;
    }

    public Localiation getLocalization() {
        return localization;
    }

    public void setLocalization(Localiation localization) {
        this.localization = localization;
    }

    public List<InputField> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(List<InputField> additionalFields) {
        this.additionalFields = additionalFields;
    }
}
