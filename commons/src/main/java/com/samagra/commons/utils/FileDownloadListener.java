package com.samagra.commons.utils;

public interface FileDownloadListener {
    void onComplete();

    void onProgress(int progress);

    void onCancelled(Exception exception);
}
