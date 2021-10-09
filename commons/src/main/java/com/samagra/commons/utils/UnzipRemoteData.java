package com.samagra.commons.utils;

import android.content.Context;
import android.widget.Toast;

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
import com.samagra.grove.logging.Grove;

public class UnzipRemoteData {
    private WeakReference<Context> contextWeakReference;
    private File outputFile;
    private InputStream inputStream = null;

    public UnzipRemoteData(Context context, int rawFileID, String path) {
        outputFile = new File(path + "/data.json");
        contextWeakReference = new WeakReference<>(context);
        this.inputStream =  context
                .getApplicationContext()
                .getResources()
                .openRawResource(rawFileID);
    }

    public UnzipRemoteData(Context context, InputStream inputStream){
        this.contextWeakReference = new WeakReference<Context>(context);
        this.inputStream = inputStream;
    }

    /**
     * This method schedules the actual method that unzips data to run on the Background computation thread.
     * A boolean value is observed indicating the success/failure of the method.
     */
    public void gunzipIt() {
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
            Grove.e("Context passed is null");
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

                return Observable.just(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                return Observable.just(false);
            }
        });
    }
}
