package com.samagra.commons.models.chaptersdata;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ChapterMapping extends RealmObject implements Serializable {
    private int grade;
    private int subjectId;
    private String competencyId;
    private String type;
    private boolean isActive;
    private int assessmentTypeId = 0;
    private RealmList<String> refIds;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getCompetencyId() {
        return competencyId;
    }

    public void setCompetencyId(String competencyId) {
        this.competencyId = competencyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public RealmList<String> getRefIds() {
        return refIds;
    }

    public void setRefIds(RealmList<String> refIds) {
        this.refIds = refIds;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public void setAssessmentTypeId(int assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
}
