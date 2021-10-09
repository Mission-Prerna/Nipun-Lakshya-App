package com.samagra.parent.ui.competencyselection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.samagra.commons.models.metadata.CompetencyModel;
import com.samagra.parent.R;

public class ReadOnlyCompetencyItemView extends FrameLayout {
    private View rootView;
    private TextView headingView;
    private TextView id;
    private ConstraintLayout clItemClicked;
    private ImageView subjectImage;

    public ReadOnlyCompetencyItemView(@NonNull Context context) {
        this(context, null);
    }

    public ReadOnlyCompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadOnlyCompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ReadOnlyCompetencyItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.item_read_only_competency, this, true);
    }

    public void setData(CompetencyModel data, int sNo) {
        headingView = ((TextView) rootView.findViewById(R.id.tv_competency));
        clItemClicked = rootView.findViewById(R.id.cl_item_clicked);
        subjectImage = rootView.findViewById(R.id.iv_subject_image);
        if (data.getSubjectId() == 1) {
            subjectImage.setImageResource(R.drawable.ic_math);
        } else {
            subjectImage.setImageResource(R.drawable.ic_hindi);
        }
        headingView.setText(data.getLearningOutcome());
    }

    /*@Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        Drawable background = clItemClicked.getBackground();
        if (selected) {

            headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            id.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }else {
            headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_2e3192));
            id.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_2e3192));
        }
    }*/
}