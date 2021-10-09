package com.samagra.ancillaryscreens.customWidgets;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.samagra.ancillaryscreens.R;
import com.samagra.ancillaryscreens.databinding.LayoutDateInputBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateInputView extends FrameLayout {
    private LayoutDateInputBinding mBinding;
    private OnDateChangeListener mListener;
    private Date selectedDate;
    private Date minDate;

    public DateInputView(@NonNull Context context) {
        this(context, null);
    }

    public DateInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DateInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DateView);
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.layout_date_input, this, true);
        mBinding.tvLabel.setText(typedArray.getString(R.styleable.DateView_label));
        mBinding.llyCalView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                if(selectedDate != null){
                    c.setTime(selectedDate);
                }
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        setSelection(dayOfMonth, month, year);
                    }
                }, mYear, mMonth, mDay);
                if(minDate != null) {
                    dialog.getDatePicker().setMinDate(minDate.getTime());
                }
                dialog.show();
            }
        });
    }

    private void setSelection(int dayOfMonth, int month, int year){
        String dayStr = "";
        if (dayOfMonth >= 10) {
            dayStr = dayOfMonth + "";
        } else {
            dayStr = "0" + dayOfMonth;
        }
        String monthStr = "";
        if (month + 1 >= 10) {
            monthStr = (month + 1) + "";
        } else {
            monthStr = "0" + (month + 1);
        }
        mBinding.tvDate.setText(dayStr + "/" + monthStr + "/" + year);
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.set(Calendar.YEAR, year);
        selectedCal.set(Calendar.MONTH, month);
        selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        selectedDate = selectedCal.getTime();

        if (mListener != null) {
            mListener.onDateChange(selectedDate);
        }
    }

    public void setSelectedDate(Date date){
        this.selectedDate = date;
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        setSelection(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
    }

    public boolean isDateSelected() {
        return selectedDate != null;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public String getSelectedDate(String pattern) {
        if (selectedDate == null){
            return "";
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(selectedDate);
    }

    public void setMinDate(Date date){
        this.minDate = date;
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        mListener = listener;
    }

    public interface OnDateChangeListener {
        void onDateChange(Date date);
    }
}
