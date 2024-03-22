package com.samagra.parent.ui.HomeScreen;

public class AssessmentRangeConfig {
    private int grade;
    private int hindi_start_index;
    private int hindi_end_index;
    private int maths_start_index;
    private int maths_end_index;

    public AssessmentRangeConfig(int grade, int hindi_start_index, int hindi_end_index, int maths_start_index, int maths_end_index) {
        this.grade = grade;
        this.hindi_start_index = hindi_start_index;
        this.hindi_end_index = hindi_end_index;
        this.maths_start_index = maths_start_index;
        this.maths_end_index = maths_end_index;
    }

    public int getGrade() {
        return grade;
    }

    public int getHindiStartIndex() {
        return hindi_start_index;
    }

    public int getHindiEndIndex() {
        return hindi_end_index;
    }

    public int getMathsStartIndex() {
        return maths_start_index;
    }

    public int getMathsEndIndex() {
        return maths_end_index;
    }
}
