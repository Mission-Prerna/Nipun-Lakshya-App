package com.samagra.parent.ui.competencyselection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.samagra.commons.models.metadata.CompetencyModel;
import com.samagra.parent.R;

public class CompetencyItemView extends FrameLayout {
    private View rootView;
    private TextView headingView;
    private TextView id;
    private ConstraintLayout clItemClicked;

    public CompetencyItemView(@NonNull Context context) {
        this(context, null);
    }

    public CompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.item_competency, this, true);
    }

    public void setData(CompetencyModel data, int sNo) {
        headingView = ((TextView) rootView.findViewById(R.id.tv_competency));
        clItemClicked = rootView.findViewById(R.id.cl_item_clicked);
        id = ((TextView) rootView.findViewById(R.id.iv_subject_image));
        id.setText(String.valueOf(sNo));
        headingView.setText(data.getLearningOutcome());
    }

}