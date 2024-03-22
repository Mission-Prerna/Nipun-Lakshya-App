package com.example.assets.uielements;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.assets.R;

public class CustomMessageDialog extends Dialog {

    private boolean loaderVisible = false;
    private boolean crossWithOneCtaVisible = false;
    private FinishListener listener;
    private CallbackListner callbackListener;
    private final String title;
    private final Drawable image;
    private final String description;
    private DismissListener dismissListener = null;
    private String btn_2_text;
    private String btn_1_text;
    private Boolean spannable = false;

    public CustomMessageDialog(@NonNull Context context, Drawable image, String title, String description) {
        super(context);
        this.title = title;
        this.image = image;
        this.description = description;
    }

    public CustomMessageDialog(@NonNull Context context, Drawable image, String title, String description, Boolean spannable) {
        super(context);
        this.title = title;
        this.image = image;
        this.description = description;
        this.spannable = spannable;
    }

    public CustomMessageDialog(@NonNull Context context, Drawable image, String title, String description, boolean loaderVisible, boolean crossWithOneCtaVisible) {
        super(context);
        this.title = title;
        this.image = image;
        this.description = description;
        this.loaderVisible = loaderVisible;
        this.crossWithOneCtaVisible = crossWithOneCtaVisible;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(R.layout.dialog_registration_success);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ((TextView) findViewById(R.id.title_tv)).setText(title);
        setDescriptionVisibility();
        setImageVisibility();
        setLoaderVisibility();
        setCrossWithOneCta();
        setDismissButtonVisibility();
        setListener();
    }

    private void setDismissButtonVisibility() {
        if (dismissListener != null) {
            findViewById(R.id.cta_button_2).setVisibility(View.VISIBLE);
            if (btn_2_text != null) {
                ((Button) findViewById(R.id.cta_button_2)).setText(btn_2_text);
            } else {
                ((Button) findViewById(R.id.cta_button_2)).setText("Dismiss");
            }
            if (btn_1_text != null) {
                ((Button) findViewById(R.id.cta_button)).setText(btn_1_text);
            } else {
                ((Button) findViewById(R.id.cta_button)).setText(R.string.okay);
            }
            findViewById(R.id.cta_button_2).setOnClickListener(v -> {
                dismissListener.onDismiss();
                dismiss();
            });
        } else {
            findViewById(R.id.cta_button_2).setVisibility(View.GONE);
        }
    }

    private void setImageVisibility() {
        if (image != null) {
            findViewById(R.id.iv_image).setVisibility(View.VISIBLE);
            ((AppCompatImageView) findViewById(R.id.iv_image)).setImageDrawable(image);
        } else {
            findViewById(R.id.iv_image).setVisibility(View.GONE);
        }
    }

    private void setLoaderVisibility() {
        if (loaderVisible) {
            findViewById(R.id.cta_button).setVisibility(View.GONE);
            findViewById(R.id.cta_button_2).setVisibility(View.GONE);
            findViewById(R.id.iv_close).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_close).setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFinish();
                }
            });
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        } else {
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
            findViewById(R.id.iv_close).setVisibility(View.GONE);
            setDismissButtonVisibility();
        }
    }

    private void setCrossWithOneCta() {
        if (crossWithOneCtaVisible) {
            findViewById(R.id.cta_button).setVisibility(View.VISIBLE);
            findViewById(R.id.cta_button_2).setVisibility(View.GONE);
            findViewById(R.id.iv_close).setVisibility(View.VISIBLE);

            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
            findViewById(R.id.iv_close).setOnClickListener(v -> {
                if (callbackListener != null) {
                    callbackListener.onFailure();
                }
            });
        }
    }

    private void setListener() {
        findViewById(R.id.cta_button).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFinish();
            } else if (callbackListener != null) {
                callbackListener.onSuccess();
            }
            dismiss();
        });
    }

    private void setDescriptionVisibility() {
        if (description != null) {
            findViewById(R.id.description_tv).setVisibility(View.VISIBLE);
            TextView desTv = (TextView) findViewById(R.id.description_tv);
            if (spannable) {
                String phoneNumber = extractPhoneNumber(description);
                SpannableString ss = new SpannableString(description);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        Uri uri = Uri.parse("tel:" + phoneNumber);
                        intent.setData(uri);
                        desTv.getContext().startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };

                if (phoneNumber.isEmpty()) {
                    desTv.setText(description);
                } else {

                    char[] charArray = phoneNumber.toCharArray();
                    int startIndex = description.indexOf(charArray[0]);
                    int lastIndex = phoneNumber.length() + startIndex;
                    ss.setSpan(clickableSpan, startIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new UnderlineSpan(), startIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    desTv.setText(ss);
                    desTv.setMovementMethod(LinkMovementMethod.getInstance());


                }
            } else {
                desTv.setText(description);
            }


        } else {
            findViewById(R.id.description_tv).setVisibility(View.GONE);
        }
    }

    public void setOnFinishListener(FinishListener listener) {
        this.listener = listener;
    }

    public void setOnFinishListener(String btn_1_text, String btn_2_text, FinishListener listener, DismissListener dismissListener) {
        this.btn_1_text = btn_1_text;
        this.btn_2_text = btn_2_text;
        this.listener = listener;
        this.dismissListener = dismissListener;
    }

    public void setOnFinishListener(String btn_1_text, CallbackListner listener) {
        this.btn_1_text = btn_1_text;
        callbackListener = listener;
    }

    public interface FinishListener {
        void onFinish();
    }

    public interface CallbackListner {
        void onSuccess();

        void onFailure();
    }

    public interface DismissListener {
        void onDismiss();
    }

    public String extractPhoneNumber(String input) {
        try {
            String[] split = input.split("\\d+");
            String withoutNumString = split[0].trim();
            String numString = input.substring(withoutNumString.length() + 1).trim();
            String[] s = numString.split(" ", 2);
            return s[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

}
