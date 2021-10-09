package com.samagra.commons.utils;

import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloaderTask extends AsyncTask<String, Integer, Exception> {

    final private String targetFile;
    private FileDownloadListener listener;

    public FileDownloaderTask(String targetFile) {
        this.targetFile = targetFile;
    }

    public void setListener(FileDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Exception doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(targetFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0)
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (e != null)
            listener.onCancelled(e);
        else
            listener.onComplete();
    }
}
