package com.samagra.commons;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DownloadProgressDialog extends Dialog {

    private FinishListener listener;
    private final String title;
    private final String description;
    private ProgressBar progressView;
    private TextView progressTv;

    public DownloadProgressDialog(@NonNull Context context, String title, String description) {
        super(context);
        this.title = title;
        this.description = description;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(R.layout.dialog_download_progress);
        progressView = (ProgressBar) findViewById(R.id.progress_view);
        progressTv = (TextView) findViewById(R.id.progress_tv);
        progressView.setIndeterminate(true);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ((TextView) findViewById(R.id.title_tv)).setText(title);
        ((TextView) findViewById(R.id.description_tv)).setText(description);

        findViewById(R.id.cta_button).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFinish();
            }
            dismiss();
        });

    }

    public void setProgress(int progressPer) {
        if (progressView != null) {
            progressView.setIndeterminate(false);
            progressTv.setVisibility(View.VISIBLE);
            progressView.setProgress(progressPer);
            progressTv.setText(progressPer + " %");
        }
    }

    public void setOnFinishListener(FinishListener listener) {
        this.listener = listener;
    }

    public interface FinishListener {
        void onFinish();
    }
}
