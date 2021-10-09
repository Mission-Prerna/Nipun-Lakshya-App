package com.samagra.commons.utils;

import android.content.Context;
import android.widget.Toast;

import com.samagra.grove.logging.Grove;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.GZIPInputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A background tasks that unzips the raw files that contain student information.
 * A listener can be added to the gunzipIt() method as a param if there is a need to listen for the
 * callbacks on the main thread.
 */
public class FileUnzipper {

    private WeakReference<Context> contextWeakReference;
    private File outputFile;
    private InputStream inputStream;
    private UnzipTaskListener unzipTaskListener;


    public FileUnzipper(Context context, String path, int resID, UnzipTaskListener unzipTaskListener) {
        outputFile = new File(path);
        //Collect.ODK_ROOT + "/data.json"
        contextWeakReference = new WeakReference<>(context);
        this.inputStream =  context
                .getApplicationContext()
                .getResources()
                .openRawResource(resID);
        this.unzipTaskListener = unzipTaskListener;
    }

    public FileUnzipper(Context context, InputStream inputStream){
        this.contextWeakReference = new WeakReference<Context>(context);
        this.inputStream = inputStream;
    }

    /**
     * This method schedules the actual method that unzips data to run on the Background computation thread.
     * A boolean value is observed indicating the success/failure of the method.
     */
    public void unzipFile() {
        if (contextWeakReference != null && contextWeakReference.get() != null) {
            Context context = contextWeakReference.get();
            final boolean[] result = {false};
            doBackgroundOps(context)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Grove.i("OnSubscribe");
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            result[0] = aBoolean;
                            Grove.i("On Next %s", aBoolean);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Grove.e("OnError %s", e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            if (!result[0]) {
                                Toast.makeText(context.getApplicationContext(), "Unable to Unzip Raw data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Grove.e("Context passed while unzipping file is null");
        }
    }

    /**
     * The actual method that is responsible for unzipping the raw data and writing it to outputFile.
     *
     * @return an {@link Observer<Boolean>} indicating the success/failure of unzip process.
     */
    private Observable<Boolean> doBackgroundOps(Context context) {
        return Observable.defer(() -> {
            byte[] buffer = new byte[1024];

            try {
                GZIPInputStream gzis = new GZIPInputStream(inputStream);
                FileOutputStream out = new FileOutputStream(outputFile);

                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }

                gzis.close();
                out.close();

                Grove.v("Unzip Done");
                if(this.unzipTaskListener != null) unzipTaskListener.unZipSuccess();
                return Observable.just(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                if(this.unzipTaskListener != null) unzipTaskListener.unZipFailure(ex);
                return Observable.just(false);
            }
        });
    }
}
