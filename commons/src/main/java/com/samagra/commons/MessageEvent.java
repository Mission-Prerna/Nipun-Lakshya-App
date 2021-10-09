package com.samagra.commons;

public class MessageEvent {
    private int totalMarks;
    private  int grade;
    private boolean isFinal;
    private String subject;

    public MessageEvent(int totalMarks, int grade, boolean isFinal, String subject) {
        this.totalMarks = totalMarks;
        this.grade = grade;
        this.isFinal = isFinal;
        this.subject = subject;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public int getGrade() {
        return grade;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getSubject() {
        return subject;
    }
}