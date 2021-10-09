package com.samagra.ancillaryscreens.customWidgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.samagra.ancillaryscreens.R;
import com.samagra.ancillaryscreens.databinding.LayoutInputBoxBinding;


/**
 * This class is used for  For InputBox in App
 * Created by hemlata.s on 02-03-2018.
 */

public class InputBox extends RelativeLayout {
    private Context mContext;
    private LayoutInputBoxBinding binding;

    public InputBox(Context context) {
        this(context, null);

    }

    public InputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    /**
     * Set custom attributes passed in xml
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.InputBoxAttrs);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_input_box, this, true);

//        setInputBoxRightArrow(typedArray.getBoolean(R.styleable.InputBoxAttrs_showRightArrow, false));
//        setInputBoxEnable(typedArray.getBoolean(R.styleable.InputBoxAttrs_isInputBoxEnable, true));
        setIsSingleLine(typedArray.getBoolean(R.styleable.InputBoxAttrs_isSingleLine, true));
//        setIsSingleLine(typedArray.getBoolean(R.styleable.InputBoxAttrs_isCursorVisible, true));
//        setIsInputVerticalScrollBarEnabled(typedArray.getBoolean(R.styleable.InputBoxAttrs_isInputVerticalScrollBarEnabled, true));
        setInputBoxEditable(typedArray.getBoolean(R.styleable.InputBoxAttrs_isEditable, true));
        setInputboxText(typedArray.getString(R.styleable.InputBoxAttrs_inputboxText));
//        setInputboxTitle(typedArray.getString(R.styleable.InputBoxAttrs_inputboxTitle));
        setInputboxHint(typedArray.getString(R.styleable.InputBoxAttrs_inputboxHint));
//        setInputboxErrorMsg(typedArray.getString(R.styleable.InputBoxAttrs_inputboxErrorMsg));
        setInputboxInputType(typedArray.getInt(R.styleable.InputBoxAttrs_android_inputType, EditorInfo.TYPE_NULL));
//        setInputboxImeOptions(typedArray.getInt(R.styleable.InputBoxAttrs_android_imeOptions, EditorInfo.TYPE_NULL));
//        setInputboxLines(typedArray.getInt(R.styleable.InputBoxAttrs_inputboxLines, 0));
//        setInputboxMinLines(typedArray.getInt(R.styleable.InputBoxAttrs_inputboxMinLines, 0));
//        setInputboxMaxLines(typedArray.getInt(R.styleable.InputBoxAttrs_inputboxMaxLines, 0));
        setInputboxMaxLength(typedArray.getInt(R.styleable.InputBoxAttrs_inputboxMaxLength, 0));
//        setInputboxLeftText(typedArray.getString(R.styleable.InputBoxAttrs_inputboxLeftText));
        setInputBoxLeftDrawable(typedArray.getDrawable(R.styleable.InputBoxAttrs_inputboxLeftImage));
//        setInputboxRightText(typedArray.getString(R.styleable.InputBoxAttrs_inputboxRightText));
//        setPaddingRight(typedArray.getDimensionPixelSize(R.styleable.InputBoxAttrs_paddingRight, 0));
        binding.textInputLayout.setPasswordVisibilityToggleEnabled(typedArray.getBoolean(R.styleable.InputBoxAttrs_passwordToggleEnabled, false));
        typedArray.recycle();
    }

    /**
     * make editbox non editable
     *
     * @param isEditable
     */
    private void setInputBoxEditable(boolean isEditable) {
        if (!isEditable) {
            binding.inputbox.setOnKeyListener(null);
            binding.inputbox.setFocusable(false);
            binding.inputbox.setCursorVisible(false);
        }
    }

//    private void setInputBoxRightArrow(boolean show) {
//        mLayoutCommonInputboxBinding.imgRightArrow.setVisibility(show?View.VISIBLE:View.GONE);
//    }
//
//    public void setInputboxRightText(String rightText) {
//        if (!TextUtils.isEmpty(rightText)) {
//            mLayoutCommonInputboxBinding.rightText.setVisibility(View.VISIBLE);
//            mLayoutCommonInputboxBinding.rightText.setText(rightText);
//        }else {
//            mLayoutCommonInputboxBinding.rightText.setVisibility(View.GONE);
//            mLayoutCommonInputboxBinding.rightText.setText("");
//        }
//        invalidate();
//    }
//
//    public void setInputboxRightTextNormal(String rightText) {
//        if (!TextUtils.isEmpty(rightText)) {
//            mLayoutCommonInputboxBinding.rightTextNormal.setVisibility(View.VISIBLE);
//            mLayoutCommonInputboxBinding.rightTextNormal.setText(rightText);
//        }else {
//            mLayoutCommonInputboxBinding.rightTextNormal.setVisibility(View.GONE);
//            mLayoutCommonInputboxBinding.rightTextNormal.setText("");
//        }
//    }

    public void setPaddingRight(int paddingRight) {
        float scale = getResources().getDisplayMetrics().density;
        int paddingLeft = (int) (getResources().getDimension(R.dimen.padding_4) * scale);
        binding.inputbox.setPadding(paddingLeft, 0, paddingRight, 0);
        binding.inputbox.setSingleLine(true);
        binding.inputbox.setMaxLines(1);
        binding.inputbox.setEllipsize(TextUtils.TruncateAt.END);
    }

    private void setInputBoxLeftDrawable(Drawable drawable) {
        if (drawable != null) {
            binding.textInputLayout.setStartIconVisible(true);
            binding.textInputLayout.setStartIconDrawable(drawable);
            binding.textInputLayout.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        } else {
            binding.textInputLayout.setStartIconDrawable(null);
            binding.textInputLayout.setStartIconVisible(false);
        }
    }

//    private void setInputboxLeftText(String leftText) {
//        if (!TextUtils.isEmpty(leftText)) {
//            mLayoutCommonInputboxBinding.leftText.setVisibility(View.VISIBLE);
//            mLayoutCommonInputboxBinding.leftText.setText(leftText);
//            this.post(new Runnable() {
//                @Override
//                public void run() {
//                    float scale = getResources().getDisplayMetrics().density;
//                    int padding = (int) (getResources().getDimension(R.dimen.padding_4) * scale);
//                    mLayoutCommonInputboxBinding.inputbox.setPadding(mLayoutCommonInputboxBinding.leftText.getWidth() + padding, 0, 0, 0);
//                }
//            });
//        }
//    }

    public void setIsCursorVisible(boolean isSingleLine) {
        binding.inputbox.setCursorVisible(isSingleLine);
    }

    public void setIsRequestFocus(boolean isSingleLine) {
        binding.inputbox.requestFocus();
    }

    public void setIsSingleLine(boolean isSingleLine) {
        binding.inputbox.setSingleLine(isSingleLine);
    }

    public void setInputboxLines(int inputboxLines) {
        if (inputboxLines != 0 && inputboxLines > 0) {
            binding.inputbox.setLines(inputboxLines);
        }
    }

    public void setInputboxDigits(String inputboxDigits) {
        if (!TextUtils.isEmpty(inputboxDigits)) {
            binding.inputbox.setKeyListener(DigitsKeyListener.getInstance(inputboxDigits));
        }
    }

    public void setInputboxMinLines(int inputboxMinLines) {
        if (inputboxMinLines != 0 && inputboxMinLines > 0) {
            binding.inputbox.setMinLines(inputboxMinLines);
        }
    }

    public void setInputboxMaxLines(int inputboxMaxLines) {
        if (inputboxMaxLines != 0 && inputboxMaxLines > 0) {
            binding.inputbox.setMaxLines(inputboxMaxLines);
        }
    }

    public void setInputboxInputType(int inputType) {
        if (EditorInfo.TYPE_NULL == inputType) {
            binding.inputbox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        } else {
            binding.inputbox.setInputType(inputType);
        }
    }


    public void setInputboxInputTypePass() {

        binding.inputbox.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        binding.inputbox.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.inputbox.setTransformationMethod(PasswordTransformationMethod.getInstance());

    }

    public void setInputboxMaxLength(int inputboxMaxLength) {
        if (inputboxMaxLength > 0) {
            binding.inputbox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputboxMaxLength)});
        }
    }

    public void setIsInputVerticalScrollBarEnabled(boolean isInputVerticalScrollBarEnabled) {
        binding.inputbox.setScroller(isInputVerticalScrollBarEnabled ? new Scroller(getContext()) : null);
        binding.inputbox.setVerticalScrollBarEnabled(isInputVerticalScrollBarEnabled);
    }

    public void setInputboxImeOptions(int imeOptions) {
        if (EditorInfo.TYPE_NULL != imeOptions) {
            binding.inputbox.setImeOptions(imeOptions);
        }
    }

    /**
     * Set inputbox title
     *
     * @param inputBoxTitle title message
     */
    public void setInputboxTitle(@NonNull String inputBoxTitle) {
        binding.textInputLayout.setHint(inputBoxTitle);
        /*if (!TextUtils.isEmpty(inputBoxTitle)) {
            mLayoutCommonInputboxBinding.inputTitle.setVisibility(VISIBLE);
            mLayoutCommonInputboxBinding.inputTitle.setText(inputBoxTitle);
        } else {
            mLayoutCommonInputboxBinding.inputTitle.setVisibility(GONE);
            mLayoutCommonInputboxBinding.inputTitle.setText("");
        }*/
    }

    /**
     * Set hint for inputbox
     *
     * @param inputBoxHint
     */
    public void setInputboxHint(@NonNull String inputBoxHint) {
        binding.textInputLayout.setHint(inputBoxHint);
    }

//    public ImageView getRightInputBoxImg(){
//        return mLayoutCommonInputboxBinding.imgRight;
//    }

//    public void setInputBoxRightImg(@NonNull int drawableID, OnClickListener onClickListener) {
//
//        mLayoutCommonInputboxBinding.imgRight.setVisibility(VISIBLE);
//        mLayoutCommonInputboxBinding.imgRight.setImageResource(drawableID);
//        if (onClickListener != null) {
//            mLayoutCommonInputboxBinding.imgRight.setOnClickListener(onClickListener);
//        }
//
//        this.post(new Runnable() {
//            @Override
//            public void run() {
//                float scale = getResources().getDisplayMetrics().density;
//                int paddingLeft = (int) (getResources().getDimension(R.dimen.padding_4) * scale);
//                int paddingRight = (int) (getResources().getDimension(R.dimen.padding_12) * scale);
//                mLayoutCommonInputboxBinding.inputbox.setPadding(paddingLeft, 0, paddingRight, 0);
//            }
//        });
//    }
//
//    public void setInputBoxRightTextClick(OnClickListener onClickListener) {
//        if (onClickListener != null) {
//            mLayoutCommonInputboxBinding.rightText.setOnClickListener(onClickListener);
//        }
//    }

    /**
     * Set Error message for inputbox
     *
     * @param inputBoxErrorMsg inputboxErrorMsg
     */
    public void setInputboxErrorMsg(@NonNull String inputBoxErrorMsg) {
        if (!TextUtils.isEmpty(inputBoxErrorMsg)) {
            binding.textInputLayout.setErrorEnabled(true);
            binding.textInputLayout.setError(inputBoxErrorMsg);
            /*binding.inputErrorMsg.setVisibility(VISIBLE);
            binding.inputErrorMsg.setText(inputBoxErrorMsg);*/
            binding.inputbox.setBackgroundResource(R.drawable.common_inputbox__error_bg_selector);
        } else {
            binding.inputbox.setBackgroundResource(R.drawable.common_inputbox_bg_selector);
            binding.textInputLayout.setErrorEnabled(false);
            binding.textInputLayout.setError("");
        }
    }

    /**
     * Get text from inputbox
     *
     * @return
     */
    public String getUserInputText() {
        return TextUtils.isEmpty(binding.inputbox.getText()) ? "" : binding.inputbox.getText().toString();
    }

    /**
     * Get text from inputbox
     *
     * @return
     */
    public boolean isDataPresent() {
        return !TextUtils.isEmpty(binding.inputbox.getText());
    }

    /**
     * Get inputbox View
     *
     * @return
     */
    public EditText getInputBoxView() {
        return binding.inputbox;
    }

    /**
     * Get right text View
     *
     * @return
     */
//    public TextView getRightTextView() {
//        return mLayoutCommonInputboxBinding.rightText;
//    }
//
//    /**
//     * Get Title View of inputbox
//     *
//     * @return
//     */
//    public TextView getInputBoxTitleView() {
//        return mLayoutCommonInputboxBinding.inputTitle;
//    }

    /**
     * get Error View of inputbox
     *
     * @return
     */
    public TextView getInputBoxErrorView() {
        return binding.inputErrorMsg;
    }

    /**
     * set state for inputbox
     *
     * @param enable
     */
    public void setInputBoxEnable(boolean enable) {
        super.setEnabled(enable);
//        mLayoutCommonInputboxBinding.inputTitle.setEnabled(enable);
        binding.inputbox.setEnabled(enable);
        if (enable) {
            binding.inputbox.clearFocus();
            binding.inputbox.setFocusable(true);
            binding.inputbox.setFocusableInTouchMode(true);

        } else {
            binding.inputbox.clearFocus();
            binding.inputbox.setFocusable(false);
            binding.inputbox.setFocusableInTouchMode(false);

        }

        binding.inputErrorMsg.setEnabled(enable);
    }

    // setter for binding
    public void setInputboxText(String text) {
        if (!TextUtils.isEmpty(text)) {
            binding.inputbox.setText(text);
        }
    }

    /**
     * Add Watcher to InputBox
     *
     * @param textWatcher
     */
    public void addInputBoxWatcher(@NonNull TextWatcher textWatcher) {
        binding.inputbox.addTextChangedListener(textWatcher);
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        binding.inputbox.setOnFocusChangeListener(listener);
    }

    public void setClickListener(OnClickListener listener) {
        binding.inputbox.setOnClickListener(listener);
    }

    public void setEditorListener(TextView.OnEditorActionListener editorListener) {
        binding.inputbox.setOnEditorActionListener(editorListener);
    }

    public void setCounterEnabled(boolean state){
        binding.textInputLayout.setCounterEnabled(state);
    }

    public void setCounterMaxLength(int maxLength){
        binding.textInputLayout.setCounterMaxLength(maxLength);
    }
}
