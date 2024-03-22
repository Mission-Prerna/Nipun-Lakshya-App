package com.samagra.grove.contracts;

import java.io.File;

public interface OverrideUploadFileCallback {
    void sendAppLogsToServer(File file);
}