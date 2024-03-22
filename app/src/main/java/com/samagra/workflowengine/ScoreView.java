package com.samagra.workflowengine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.samagra.parent.R;
import com.samagra.parent.databinding.ViewScoreBinding;


public class ScoreView extends FrameLayout {
    private ViewScoreBinding mBinding;

    public ScoreView(@NonNull Context context) {
        this(context, null);
    }

    public ScoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_score, this, true);
    }

    public void setData(String studentName, int requiredWordsPerMinute, int resultWordsPerMinute, boolean isFluencyTest) {
        setResults(studentName, requiredWordsPerMinute, resultWordsPerMinute, isFluencyTest);
        if (resultWordsPerMinute >= requiredWordsPerMinute) {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
            mBinding.txvScore.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
        } else {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.red_8b0000));
            mBinding.txvScore.setTextColor(ContextCompat.getColor(getContext(), R.color.red_8b0000));
        }
    }

    public void setPercentageData(String studentName, int requiredPercentage, int resultPercentage) {
        mBinding.txvTargetPer.setVisibility(VISIBLE);
        mBinding.txvScorePer.setVisibility(VISIBLE);
        mBinding.txvTarget.setVisibility(GONE);
        mBinding.txvScore.setVisibility(GONE);
        mBinding.txvScoreTitle.setText(String.format(getContext().getString(R.string.student_result), studentName));
        mBinding.txvTargetPer.setText(String.format(getContext().getString(R.string.correct_answers), requiredPercentage));
        mBinding.txvScorePer.setText(String.format(getContext().getString(R.string.correct_answers), resultPercentage));
        if (resultPercentage >= requiredPercentage) {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
            mBinding.txvScorePer.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
        } else {
            mBinding.txvScorePer.setTextColor(ContextCompat.getColor(getContext(), R.color.red_500));
            mBinding.txvScore.setTextColor(ContextCompat.getColor(getContext(), R.color.red_500));
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
    }

    public void setData(String studentName, int requiredWordsPerMinute, int resultWordsPerMinute, int requiredPercentage, int resultPercentage) {
        mBinding.txvTargetPer.setVisibility(VISIBLE);
        mBinding.txvScorePer.setVisibility(VISIBLE);
        mBinding.txvScoreTitle.setText(String.format(getContext().getString(R.string.student_result), studentName));
        mBinding.txvTarget.setText(String.format(getContext().getString(R.string.words_per_minute), requiredWordsPerMinute));
        mBinding.txvScore.setText(String.format(getContext().getString(R.string.words_per_minute), resultWordsPerMinute));
        mBinding.txvTargetPer.setText(String.format(getContext().getString(R.string.correct_answers), requiredPercentage));
        mBinding.txvScorePer.setText(String.format(getContext().getString(R.string.correct_answers), resultPercentage));
        if (resultPercentage >= requiredPercentage) {
            mBinding.txvScorePer.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
        } else {
            mBinding.txvScorePer.setTextColor(ContextCompat.getColor(getContext(), R.color.red_500));
        }
        if (resultWordsPerMinute >= requiredWordsPerMinute) {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
            mBinding.txvScore.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
        } else {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.red_8b0000));
            mBinding.txvScore.setTextColor(ContextCompat.getColor(getContext(), R.color.red_8b0000));
        }
    }

    private void setResults(String studentName, int requiredWordsPerMinute, int resultWordsPerMinute, boolean isFluencyTest) {
        mBinding.txvScoreTitle.setText(String.format(getContext().getString(R.string.student_result), studentName));
        if (isFluencyTest) {
            mBinding.txvTarget.setText(String.format(getContext().getString(R.string.words_per_minute), requiredWordsPerMinute));
            mBinding.txvScore.setText(String.format(getContext().getString(R.string.words_per_minute), resultWordsPerMinute));
        } else {
            mBinding.txvTarget.setText(String.format(getContext().getString(R.string.correct_words), requiredWordsPerMinute));
            mBinding.txvScore.setText(String.format(getContext().getString(R.string.correct_words), resultWordsPerMinute));
        }
    }


    public void setResultData(int resultWordCount, Integer requiredWordCount) {
        if (resultWordCount >= requiredWordCount) {
//            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.green_500));
        } else {
            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_yellow_bf9000));
//            mBinding.txvScoreTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.red_8b0000));
        }
        mBinding.llScoreView.setVisibility(View.GONE);
        mBinding.llScore.setVisibility(View.VISIBLE);
        mBinding.txvScore.setVisibility(View.VISIBLE);
        mBinding.llScore.setVisibility(View.VISIBLE);
        mBinding.txvScoreTitle.setText(String.valueOf(resultWordCount));
        mBinding.txvScore.setText(R.string.words_read_correct);


    }
}
