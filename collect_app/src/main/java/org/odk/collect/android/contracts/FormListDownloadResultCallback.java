package org.odk.collect.android.contracts;


import org.odk.collect.android.formmanagement.ServerFormDetails;

import java.util.HashMap;

public interface FormListDownloadResultCallback {
    void onSuccessfulFormListDownload(HashMap<String, ServerFormDetails> value);
    void onFailureFormListDownload(boolean isAPIFailure);
}
