package com.samagra.commons.firebase;

public interface IFirebaseRemoteStorageFileDownloader {
    void onFirebaseRemoteStorageFileDownloadSuccess();

    void onFirebaseRemoteStorageFileDownloadFailure(Exception exception);

    void onFirebaseRemoteStorageFileDownloadProgressState(long taskSnapshot);
}
