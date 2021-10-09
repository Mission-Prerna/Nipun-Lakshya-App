package com.data.models.stateresult;

import com.samagra.commons.models.OdkResultData;
import com.samagra.commons.models.schoolsresponsedata.SchoolsData;

import java.io.Serializable;

import javax.annotation.Nullable;

public class AssessmentStateResult extends StateResult implements Serializable {
    private Integer grade;
    private String studentId;
    private String section;
    private String subject;
    private String competency;
    private String competencyId;
    private OdkResultData odkResultsData;
    private SchoolsData schoolsData;
    private String studentName;
    private String workflowRefId;
    private Integer currentStudentCount;
    private ModuleResult moduleResult;
    private String studentSession;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ModuleResult getModuleResult() {
        return moduleResult;
    }

    public void setModuleResult(ModuleResult moduleResult) {
        this.moduleResult = moduleResult;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public SchoolsData getSchoolsData() {
        return schoolsData;
    }

    public void setSchoolsData(SchoolsData schoolsData) {
        this.schoolsData = schoolsData;
    }

    public String getCompetency() {
        return competency;
    }

    public void setCompetency(String competency) {
        this.competency = competency;
    }

    public Integer getCurrentStudentCount() {
        return currentStudentCount;
    }

    public void setCurrentStudentCount(Integer currentStudentCount) {
        this.currentStudentCount = currentStudentCount;
    }

    @Nullable
    public OdkResultData getOdkResultsData() {
        return odkResultsData;
    }

    public void setOdkResultsData(OdkResultData odkResultsData) {
        this.odkResultsData = odkResultsData;
    }

    public String getCompetencyId() {
        return competencyId;
    }

    public void setCompetencyId(String competencyId) {
        this.competencyId = competencyId;
    }

    public String getStudentSession() {
        return studentSession;
    }

    public void setStudentSession(String studentSession) {
        this.studentSession = studentSession;
    }

    public void setWorkflowRefId(String workflowRefId) {
        this.workflowRefId = workflowRefId;
    }

    public String getWorkflowRefId() {
        return workflowRefId;
    }
}
