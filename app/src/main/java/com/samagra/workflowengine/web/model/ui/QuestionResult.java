package com.samagra.workflowengine.web.model.ui;

import java.util.List;

public class QuestionResult {
    private String question;
    private String correctAnswer;
    private String userAnswer;
    private String doID;
    private boolean isCorrectAnswer;
    private List<String> options;
    private String answerStatus;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = sanitize(question);
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = sanitize(correctAnswer);
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = sanitize(userAnswer);
    }

    public String getDoID() {
        return doID;
    }

    public void setDoID(String doID) {
        this.doID = doID;
    }

    public boolean isCorrectAnswer() {
        return isCorrectAnswer;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        isCorrectAnswer = correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String sanitize(String s){
        return s.replace("\n","");
    }

    public String getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(String answerStatus) {
        this.answerStatus = answerStatus;
    }
}
