package com.samagra.commons.models;

public class FormStructure {
    private String FormID;

    public String getFormID() {
        return FormID;
    }

    public String getFormName() {
        return FormName;
    }

    public String getSubject() {
        return Subject;
    }

    private String FormName;
    private String Subject;

    public FormStructure(String formID, String formName,String subject) {
        FormID = formID;
        FormName = formName;
        Subject = subject;
    }

}
