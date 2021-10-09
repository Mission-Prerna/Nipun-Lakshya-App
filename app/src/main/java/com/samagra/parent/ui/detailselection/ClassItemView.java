package com.samagra.parent.ui.detailselection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.samagra.parent.R;

public class ClassItemView extends FrameLayout {
    private View rootView;
    private TextView headingView;

    public ClassItemView(@NonNull Context context) {
        this(context, null);
    }

    public ClassItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClassItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_class_item, this);
    }

    public void setData(String data) {
        headingView = ((TextView) rootView.findViewById(R.id.txv_heading));
        headingView.setText(data);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
           headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }else {
            headingView.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_2e3192));
        }
    }
}