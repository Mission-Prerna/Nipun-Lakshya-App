package com.samagra.workflowengine.bolo;

import com.samagra.commons.models.schoolsresponsedata.SchoolsData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ReadAlongProperties implements Serializable {
    private boolean showDisclaimer = true;
    private boolean showResults = true;
    private boolean showInstructions = true;
    private Integer grade;
    private String subject;
    private String student;
    private Integer requiredWords;
    private Integer stateGrade;
    private Integer studentCount;
    private SchoolsData schoolData;
    private String bookId;
    private List<String> bookIdList;
    private String competencyName;
    private String competencyId;
    private Date startTime;
    private boolean checkFluency = true;

    public boolean shouldShowResults() {
        return showResults;
    }

    public void setShowResults(boolean showResults) {
        this.showResults = showResults;
    }

    public boolean shouldShowDisclaimer() {
        return showDisclaimer;
    }

    public void setShowDisclaimer(boolean showDisclaimer) {
        this.showDisclaimer = showDisclaimer;
    }

    public boolean shouldShowInstructions() {
        return showInstructions;
    }

    public void setShowInstructions(boolean showInstructions) {
        this.showInstructions = showInstructions;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setStudent(String s) {
        student = s;
    }

    public String getStudent() {
        return student;
    }

    public Integer getRequiredWords() {
        return requiredWords;
    }

    public void setRequiredWords(Integer requiredWords) {
        this.requiredWords = requiredWords;
    }

    //todo to ask Preet to clean this with bookId
    @Override
    public String toString() {
        return "ReadAlongProperties{" +
                ", student='" + student + '\'' +
                ", requiredWordsPerMinute=" + requiredWords +
                ", bookId=" + bookId +
                '}';
    }

    public void setStateGrade(Integer stateGrade) {
        this.stateGrade = stateGrade;
    }

    public Integer getStateGrade() {
        return stateGrade;
    }

    public boolean isCheckFluency() {
        return checkFluency;
    }

    public void setCheckFluency(boolean checkFluency) {
        this.checkFluency = checkFluency;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public SchoolsData getSchoolData() {
        return schoolData;
    }

    public void setSchoolData(SchoolsData schoolData) {
        this.schoolData = schoolData;
    }

    public String getCompetencyName() {
        return competencyName;
    }

    public void setCompetencyName(String competencyName) {
        this.competencyName = competencyName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public List<String> getBookIdList() {
        return bookIdList;
    }

    public void setBookIdList(List<String> bookIdList) {
        this.bookIdList = bookIdList;
    }

    public String getCompetencyId() {
        return competencyId;
    }

    public void setCompetencyId(String competencyId) {
        this.competencyId = competencyId;
    }
}
