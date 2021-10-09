package com.samagra.parent.ui.formlite.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.samagra.parent.R;
import com.samagra.parent.databinding.ViewSpinnerLayoutBinding;
import com.samagra.parent.ui.formlite.model.InputField;


public class AppSpinner extends RelativeLayout {
    private static final String STR_TAG = AppSpinner.class.getName();
    private Context mContext;
    private ViewSpinnerLayoutBinding mViewSpinnerLayoutBinding;
    //    private ArrayList<AppSpinner> assistSpinnerModels = new ArrayList<>();
//    private BottomSheetBehavior mBottomSheetBehavior;
    private String identifier;
    private AdapterView.OnItemSelectedListener listener;

    public AppSpinner(Context context) {
        this(context, (AttributeSet) null);

    }

    public AppSpinner(Context context, String identifier) {
        this(context, (AttributeSet) null);
        this.identifier = identifier;
        setTag(identifier);
    }


    public AppSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public AppSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    /**
     * Set custom attributes passed in xml
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SpinnerAttrs);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mViewSpinnerLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.view_spinner_layout, this, true);

        setTitle(typedArray.getString(R.styleable.SpinnerAttrs_customLabel));
        if(typedArray.hasValue(R.styleable.SpinnerAttrs_isSmall)){
//            float paddingVertical  = typedArray.getDimension(R.styleable.SpinnerAttrs_customLabel, getResources().getDimension(R.dimen.spacing_8));
//            mViewSpinnerLayoutBinding.llyParent.setPadding(0, (int)paddingVertical, 0, (int)paddingVertical);
//            mViewSpinnerLayoutBinding.llyParent.setPadding(0, 0, 0, 0);
            mViewSpinnerLayoutBinding.llySpinnerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,110));


        }

    }

    public void setData(InputField inputField) {
        if (TextUtils.isEmpty(inputField.getErrorMessage())) {
            mViewSpinnerLayoutBinding.inputErrorMsg.setVisibility(GONE);
        } else {
            mViewSpinnerLayoutBinding.inputErrorMsg.setVisibility(VISIBLE);
            mViewSpinnerLayoutBinding.inputErrorMsg.setText(inputField.getErrorMessage());
        }
    }

    public void setSelection(int selectedPosition){
        mViewSpinnerLayoutBinding.spinnerInputbox.setSelection(selectedPosition);
    }

    public void setAdapter(ArrayAdapter adapter) {
        mViewSpinnerLayoutBinding.spinnerInputbox.setAdapter(adapter);
    }

    public void setItemSelectionListener(AdapterView.OnItemSelectedListener listener) {
        mViewSpinnerLayoutBinding.spinnerInputbox.setOnItemSelectedListener(listener);
    }

    public Object getSelectedItem() {
        return mViewSpinnerLayoutBinding.spinnerInputbox.getAdapter()
                .getItem(mViewSpinnerLayoutBinding.spinnerInputbox.getSelectedItemPosition());
    }

    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            mViewSpinnerLayoutBinding.txvTitle.setVisibility(GONE);
        } else {
            mViewSpinnerLayoutBinding.txvTitle.setVisibility(VISIBLE);
            mViewSpinnerLayoutBinding.txvTitle.setText(title);
        }
    }

    public void setIsEnable(boolean enable){
        mViewSpinnerLayoutBinding.spinnerInputbox.setEnabled(enable);
    }


//    public void isDisableClick(boolean state) {
//        mViewSpinnerLayoutBinding.oneassistSpinner.setClickable(!state);
//        mViewSpinnerLayoutBinding.oneassistSpinner.setFocusable(!state);
//        mViewSpinnerLayoutBinding.oneassistSpinner.setEnabled(!state);
//        if (state) {
//            mViewSpinnerLayoutBinding.oneassistSpinner.setOnClickListener(null);
//        } else {
//            mViewSpinnerLayoutBinding.oneassistSpinner.setOnClickListener(onClickListener);
//        }
//    }

//    @Override
//    public boolean performClick() {
//        super.performClick();
//        mViewSpinnerLayoutBinding.oneassistSpinner.performClick();
//        return true;
//    }

//    public MutableLiveData<OneAssistSpinnerModel> getSelectedItem() {
//        return selectedItem;
//    }

//    public void setHint(@StringRes int strHintRes) {
//        mViewSpinnerLayoutBinding.spinnerInputbox.setHint(strHintRes);
//    }

//    public void setHint(@NonNull String strHint) {
//        mViewSpinnerLayoutBinding.spinnerInputbox.setText(null);
//        mViewSpinnerLayoutBinding.spinnerInputbox.setHint(strHint);
//        int paddingLeft = getResources().getDimensionPixelOffset(R.dimen.padding_16);
//        mViewSpinnerLayoutBinding.spinnerInputbox.setPadding(paddingLeft, 0, 0, 0);
//    }

//    public void setSelectedItemText(@NonNull String strSpinnerSelectedText) {
//        int paddingLeft = getResources().getDimensionPixelOffset(R.dimen.padding_16);
//        mViewSpinnerLayoutBinding.spinnerInputbox.setPadding(paddingLeft, 0, 0, 0);
//        mViewSpinnerLayoutBinding.spinnerInputbox.setText(strSpinnerSelectedText);
//        mViewSpinnerLayoutBinding.inputErrorMsg.setVisibility(INVISIBLE);
//        setSpinnerErrorMsg(null);
//    }

    public void setSpinnerErrorMsg(@NonNull String strErrorMsg) {
        if (!TextUtils.isEmpty(strErrorMsg)) {
            mViewSpinnerLayoutBinding.inputErrorMsg.setText(strErrorMsg);
            mViewSpinnerLayoutBinding.inputErrorMsg.setVisibility(VISIBLE);
        } else {
            mViewSpinnerLayoutBinding.inputErrorMsg.setText(null);
            mViewSpinnerLayoutBinding.inputErrorMsg.setVisibility(INVISIBLE);
        }
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mViewSpinnerLayoutBinding.spinnerInputbox.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), TextUtils.isEmpty(strErrorMsg) ? R.drawable.spinner_bg_dropdown_type_one : R.drawable.spinner_bg_dropdown_type_error));
        } else {
            mViewSpinnerLayoutBinding.spinnerInputbox.setBackground(ContextCompat.getDrawable(getContext(), TextUtils.isEmpty(strErrorMsg) ? R.drawable.spinner_bg_dropdown_type_one : R.drawable.spinner_bg_dropdown_type_error));
        }
        int paddingLeft = getResources().getDimensionPixelOffset(R.dimen.spacing_16);
        mViewSpinnerLayoutBinding.spinnerInputbox.setPadding(paddingLeft, 0, 0, 0);
    }

//    public void setAdapterData(@NonNull SpinnerAdapter adapter) {
//        mAdapter = adapter;
//
//    }

}
