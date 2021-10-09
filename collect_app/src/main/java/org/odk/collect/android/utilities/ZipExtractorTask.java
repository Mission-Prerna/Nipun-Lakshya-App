package org.odk.collect.android.utilities;

import android.os.AsyncTask;

import org.odk.collect.android.listeners.ZipExtractListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractorTask extends AsyncTask<Void, Integer, String> {

    private final String zipPath;
    private final String targetDirectory;
    private ZipExtractListener listener;

    public ZipExtractorTask(String zipPath, String targetDirectory, ZipExtractListener listener) {
        this.zipPath = zipPath;
        this.targetDirectory = targetDirectory;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath)));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onComplete(s);
    }
}
