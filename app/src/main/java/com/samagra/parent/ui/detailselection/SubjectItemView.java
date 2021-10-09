package com.samagra.parent.ui.detailselection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.samagra.parent.R;

public class SubjectItemView extends FrameLayout {
    private View rootView;
    private TextView headingView;
    private ImageView imageView;

    public SubjectItemView(@NonNull Context context) {
        this(context, null);
    }

    public SubjectItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubjectItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SubjectItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_subject_item, this,true);
    }

    public void setData(String title, int image) {
        headingView = ((TextView) rootView.findViewById(R.id.txv_heading));
        imageView = ((ImageView) rootView.findViewById(R.id.iv_subject));

        String firstLetter = title.substring(0, 1);
        String remainingLetters = title.substring(1);
        firstLetter = firstLetter.toUpperCase();
        String name = firstLetter + remainingLetters;
        if (title.equalsIgnoreCase("hindi")) {
            headingView.setText(getContext().getString(R.string.bhasha));
        } else if (title.equalsIgnoreCase("math")) {
            headingView.setText(getContext().getString(R.string.math));
        }else{
            headingView.setText(name);
        }
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), image));

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            DrawableCompat.setTint(
                    DrawableCompat.wrap(imageView.getDrawable()),
                    ContextCompat.getColor(imageView.getContext(), R.color.white)
            );
        }else {
            headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_2e3192));
            DrawableCompat.setTint(
                    DrawableCompat.wrap(imageView.getDrawable()),
                    ContextCompat.getColor(imageView.getContext(), R.color.blue_2e3192)
            );
        }
    }

}

