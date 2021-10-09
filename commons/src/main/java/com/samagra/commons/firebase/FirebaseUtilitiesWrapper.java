package com.samagra.commons.firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.samagra.commons.R;

import java.io.File;
import java.io.FileNotFoundException;

public class FirebaseUtilitiesWrapper {

    public static void downloadFile(String storagePath, IFirebaseRemoteStorageFileDownloader iFirebaseRemoteStorageFileDownloader){
        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("samagra-data-management.appspot.com");
//        StorageReference dataRef = storageRef.child("data.json.gzip");
        File outputFile = new File(storagePath);

        StorageReference mFindPdfStorageReference =  storage.getReferenceFromUrl("gs://samagra-data-management.appspot.com/o/data.json.gzip");
        mFindPdfStorageReference.getFile(outputFile)
                .addOnSuccessListener(taskSnapshot -> iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadSuccess())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadFailure(exception);

//                        Grove.e(TAG, "Data failed to download");
//                        if (BuildConfig.FLAVOR.equals("shikshaSathi")) {
//                            if ((loadValuesToMemory() == null)) {
//                                Grove.e("Error in parsing GP file");
//                                tryCount++;
//                                if (tryCount < 2)
//                                    showFormDownloadingMessage("Failed to update data file.", 2);
//                                else {
//                                    showFormDownloadingMessage("Could not get updated Data. Using old file.", 1);
//                                    new UnzipDataTask(HomeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                }
//                            }
//                        }
                    }
                })
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        long progressPercentage = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        iFirebaseRemoteStorageFileDownloader.onFirebaseRemoteStorageFileDownloadProgressState(progressPercentage);

//
//                        Grove.e("On progress", "Data downloading " + progressPercentage);
                    }
                });
    }

    public static void downloadFormsZip(File targetFile, IFirebaseRemoteStorageFileDownloader listener, Context context) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference zipStorage = storage.getReference().child("FormsZip/forms_data.zip");
        zipStorage.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long totalBytes = storageMetadata.getSizeBytes();
                long freeBytes = targetFile.getFreeSpace();
                final long bufferSpace = 200L * 1048576L;   // 200 MB
                if (freeBytes < totalBytes + bufferSpace) {
                    listener.onFirebaseRemoteStorageFileDownloadFailure(
                            new FileNotFoundException(context.getResources().getString(R.string.low_on_space_error))
                    );
                }
                else {
                    zipStorage.getFile(targetFile)
                            .addOnProgressListener(snapshot -> listener.onFirebaseRemoteStorageFileDownloadProgressState((snapshot.getBytesTransferred()/snapshot.getTotalByteCount()) * 100))
                            .addOnSuccessListener(task -> listener.onFirebaseRemoteStorageFileDownloadSuccess())
                            .addOnFailureListener(listener::onFirebaseRemoteStorageFileDownloadFailure);
                }
            }
        }).addOnFailureListener(listener::onFirebaseRemoteStorageFileDownloadFailure);
    }
}
