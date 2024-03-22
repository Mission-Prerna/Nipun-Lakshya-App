package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Question {

    @SerializedName("subject")
    @Expose
    private List<String> subject = null;
    @SerializedName("responseDeclaration")
    @Expose
    private ResponseDeclaration responseDeclaration;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("editorState")
    @Expose
    private EditorState editorState;
    @SerializedName("gradeLevel")
    @Expose
    private List<String> gradeLevel = null;
    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("solutions")
    @Expose
    private List<Object> solutions = null;
    @SerializedName("qType")
    @Expose
    private String qType;
    @SerializedName("languageCode")
    @Expose
    private List<String> languageCode = null;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("medium")
    @Expose
    private List<String> medium = null;
    @SerializedName("media")
    @Expose
    private List<Object> media = null;
    @SerializedName("answer")
    @Expose
    private String answer;
    @SerializedName("board")
    @Expose
    private String board;

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public ResponseDeclaration getResponseDeclaration() {
        return responseDeclaration;
    }

    public void setResponseDeclaration(ResponseDeclaration responseDeclaration) {
        this.responseDeclaration = responseDeclaration;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public EditorState getEditorState() {
        return editorState;
    }

    public void setEditorState(EditorState editorState) {
        this.editorState = editorState;
    }

    public List<String> getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(List<String> gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<Object> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Object> solutions) {
        this.solutions = solutions;
    }

    public String getqType() {
        return qType;
    }

    public void setqType(String qType) {
        this.qType = qType;
    }

    public List<String> getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(List<String> languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMedium() {
        return medium;
    }

    public void setMedium(List<String> medium) {
        this.medium = medium;
    }

    public List<Object> getMedia() {
        return media;
    }

    public void setMedia(List<Object> media) {
        this.media = media;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

}
