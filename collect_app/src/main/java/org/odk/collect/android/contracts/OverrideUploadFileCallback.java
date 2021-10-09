package org.odk.collect.android.contracts;

import java.io.File;

public interface OverrideUploadFileCallback {
    void sendAppLogsToServer(File file);
}