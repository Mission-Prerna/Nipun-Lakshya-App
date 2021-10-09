package com.samagra.ancillaryscreens.customWidgets;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samagra.ancillaryscreens.R;

import timber.log.Timber;

public class InputFieldWidget extends TextInputLayout {

    private String characters;

    enum InputType {
        TEXT, NUMBER, DECIMAL, SPECIFIC
    }

    private final int MARGIN_TOP = 8; // in dp
    private TextInputEditText editText;
    private String identifier;

    public InputFieldWidget(@NonNull Context context) {
        this(context, (AttributeSet) null);
    }

    public InputFieldWidget(@NonNull Context context, String identifier) {
        this(context, (AttributeSet) null);
        this.identifier = identifier;
        setTag(identifier);
    }

    public InputFieldWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputFieldWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Resources r = getContext().getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                MARGIN_TOP,
                r.getDisplayMetrics()
        );
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, px, 0, px);

        setLayoutParams(layoutParams);
        editText = new TextInputEditText(new ContextThemeWrapper(getContext(), R.style.EditTextAppStyle));
        if (!TextUtils.isEmpty(identifier)) {
            editText.setTag(identifier);
        }
        LayoutParams editTextParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        editText.setLayoutParams(editTextParams);
        editText.setMaxLines(1);
        editText.setSingleLine(true);
//        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.card_view_et));
        addView(editText, editTextParams);
    }

    public TextInputEditText getEditableField() {
        return editText;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        if (!TextUtils.isEmpty(identifier) && editText != null) {
            editText.setTag(identifier);
        }
    }

    public void setTextChangeListener(TextWatcher textChangeListener) {
        if (editText != null)
            editText.addTextChangedListener(textChangeListener);
    }

    public void setInputType(InputType inputType) {

        if (editText != null)
            switch (inputType) {
                case NUMBER:
                    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                    break;
                case DECIMAL:
                    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                case TEXT:
//                    editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_CLASS_NUMBER);
                    editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    break;
                case SPECIFIC:
                    if (characters != null) {
                        editText.setKeyListener(DigitsKeyListener.getInstance(characters));
                    } else {
                        Timber.i("Input type - \"specific\", set without mentioning eligible digits");
                    }
                    editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    break;
            }
    }

    public void setEligibleDigits(String characters) {
        this.characters = characters;
       /* if (editText != null)
            editText.setKeyListener(DigitsKeyListener.getInstance(characters));*/
    }

//    public void setData(String formField) {
//        if (!TextUtils.isEmpty(formField.getEligibleDigits())) {
//            setEligibleDigits(formField.getEligibleDigits());
//        }
//        if (!TextUtils.isEmpty(formField.getInputType())) {
//            try {
//                InputType inputType = InputType.valueOf(formField.getInputType().toUpperCase());
//                setInputType(inputType);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (!TextUtils.isEmpty(formField.getLabel())) {
//            setHint(formField.getLabel());
//        }
//    }
}
