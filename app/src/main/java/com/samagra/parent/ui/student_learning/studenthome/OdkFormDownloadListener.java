package com.samagra.parent.ui.student_learning.studenthome;

import com.samagra.grove.logging.Grove;

import org.odk.collect.android.OdkFormsDownloadInLocalResponseData;
import org.odk.collect.android.contracts.DataFormDownloadResultCallback;
import org.odk.collect.android.formmanagement.ServerFormDetails;

import java.util.HashMap;

public class OdkFormDownloadListener implements DataFormDownloadResultCallback {
    private final OdkResponseListener listener;

    public OdkFormDownloadListener(OdkResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public void formsDownloadingSuccessful(HashMap<ServerFormDetails, String> result) {
        Grove.d("Form Download Complete %s", result);
        listener.renderLayoutVisible("Form Download Complete", 1);
    }

    @Override
    public void formsDownloadingFailure(OdkFormsDownloadInLocalResponseData data) {
        Grove.d("Unable to download the forms");
        listener.renderLayoutVisible("Unable to download the forms", 0);
        listener.onFailure(data);
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        Grove.v("Form Download InProgress = " + currentFile + " Progress" + progress + " Out of=" + total);
        Grove.d(" Total%s", String.valueOf(total));
        Grove.d(" Total Progress %s", String.valueOf(progress));
        int formProgress = (progress * 100) / total;
        if (listener != null) {
            listener.onUpdateLoaderStatus(formProgress);
        }
        Grove.d("Form Download Progress: %s", formProgress);
        if (formProgress == 100) {
            Grove.d("Rendering UI Visible as forms already downloaded not, but now downloaded");
        }
    }

    @Override
    public void formsDownloadingCancelled() {
        listener.renderLayoutVisible("Form Download Cancelled", 0);
        listener.showFailureDownloadMessage();
        Grove.e("Form Download Cancelled >> API Cancelled callback received");
    }
}
