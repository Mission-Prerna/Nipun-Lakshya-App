package com.assessment.flow.workflowengine.spinner;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.assessment.R;
import com.assessment.databinding.LayoutSpinnerBinding;


public class SpinnerFieldWidget extends FrameLayout {
    private Context mContext;
    private LayoutSpinnerBinding mBinding;
    private SelectionCallback mListener;
    private int selectedPosition = 0;
    private ArrayAdapter<String> listAdapter;

    public SpinnerFieldWidget(@NonNull Context context) {
        this(context, (AttributeSet) null);
    }

    public SpinnerFieldWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinnerFieldWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_spinner, this, true);
    }

    public void setSelectedPosition(int position) {
        mBinding.spn.setSelection(position);
        selectedPosition = position;
        if (position > 0) {
            if (mListener != null) {
                mListener.onItemClick(listAdapter.getItem(position - 1), position - 1);
            }
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedItem() {
        if (selectedPosition > 0) {
            return listAdapter.getItem(selectedPosition - 1);
        } else {
            return null;
        }
    }

    public void setSelectionCallback(SelectionCallback listener) {
        mListener = listener;
    }

    public void setListData(String[] list, String label, boolean setLabel, int textResId) {
        if (setLabel) {
            mBinding.txvLabel.setText(String.format("%s :", mBinding.txvLabel.getContext().getString(textResId)));
            mBinding.txvLabel.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mBinding.spn.getLayoutParams();
            // Set spinner margin (Left Top Right Bottom)
            lp.setMargins(10, 0, 0, 0);

            // Update parameters to spinner
            mBinding.spn.setLayoutParams(lp);
        } else {
            mBinding.txvLabel.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mBinding.spn.getLayoutParams();
            // Set spinner margin (Left Top Right Bottom)
            lp.setMargins(0, 0, 0, 0);
            // Update parameters to spinner
            mBinding.spn.setLayoutParams(lp);

        }
        listAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_textview, list);
        String labelText = !label.isEmpty() ? label : "Select ";
        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(
                listAdapter, R.layout.spinner_widget_nothing_selected, labelText,
                mContext);
        mBinding.spn.setAdapter(customSpinnerAdapter);

        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelected(int position, View view) {
                selectedPosition = position;
                if (position > 0) {
                    if (mListener != null) {
                        mListener.onItemClick(listAdapter.getItem(position - 1), position - 1);
                    }
                }
            }
        };

        mBinding.spn.setOnTouchListener(spinnerInteractionListener);
        mBinding.spn.setOnItemSelectedListener(spinnerInteractionListener);
    }

    public int getCount() {
        return listAdapter.getCount();
    }

    public void clearData() {
        listAdapter.clear();
    }

    public interface SelectionCallback {
        void onItemClick(String item, int position);

        default void onInfoButtonClick(String info) {

        }
    }
}
