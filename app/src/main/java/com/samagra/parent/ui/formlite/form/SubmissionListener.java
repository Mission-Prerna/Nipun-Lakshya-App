package com.samagra.parent.ui.formlite.form;

import java.util.Map;

public interface SubmissionListener {
    void onFormSubmitted(Map<String, Object> responses);
    void onError();
}
