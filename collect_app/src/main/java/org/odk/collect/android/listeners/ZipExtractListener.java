package org.odk.collect.android.listeners;

public interface ZipExtractListener {
    void onProgress(int progress);
    void onComplete(String error);
}
